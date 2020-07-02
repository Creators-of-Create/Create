package com.simibubi.create.content.logistics.block.realityFunnel;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.logistics.block.chute.ChuteBlock;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InsertingBehaviour;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class RealityFunnelBlock extends ProperDirectionalBlock implements ITE<RealityFunnelTileEntity> {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public RealityFunnelBlock(Properties p_i48415_1_) {
		super(p_i48415_1_);
		setDefaultState(getDefaultState().with(POWERED, false));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction facing = context.getFace();
		if (facing.getAxis()
			.isVertical()
			&& context.getWorld()
				.getBlockState(context.getPos()
					.offset(facing.getOpposite()))
				.getBlock() instanceof ChuteBlock)
			facing = facing.getOpposite();
		return getDefaultState().with(FACING, facing)
			.with(POWERED, context.getWorld()
				.isBlockPowered(context.getPos()));
	}

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		if (worldIn.isRemote)
			return;
		if (!(entityIn instanceof ItemEntity))
			return;
		if (state.get(POWERED))
			return;
		ItemEntity itemEntity = (ItemEntity) entityIn;

		Direction direction = state.get(FACING);
		Vec3d diff = entityIn.getPositionVec()
			.subtract(VecHelper.getCenterOf(pos));
		double projectedDiff = direction.getAxis()
			.getCoordinate(diff.x, diff.y, diff.z);
		if (projectedDiff < 0 == (direction.getAxisDirection() == AxisDirection.POSITIVE))
			return;

		ItemStack toInsert = itemEntity.getItem();
		ItemStack remainder = tryInsert(worldIn, pos, toInsert, false);
		
		if (remainder.isEmpty())
			itemEntity.remove();
		if (remainder.getCount() < toInsert.getCount())
			itemEntity.setItem(remainder);
	}

	public static ItemStack tryInsert(World worldIn, BlockPos pos, ItemStack toInsert, boolean simulate) {
		FilteringBehaviour filter = TileEntityBehaviour.get(worldIn, pos, FilteringBehaviour.TYPE);
		InsertingBehaviour inserter = TileEntityBehaviour.get(worldIn, pos, InsertingBehaviour.TYPE);
		if (inserter == null)
			return toInsert;
		if (filter != null && !filter.test(toInsert))
			return toInsert;
		ItemStack remainder = inserter.insert(toInsert, simulate);
		return remainder;
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.REALITY_FUNNEL.create();
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder.add(POWERED));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
		return AllShapes.REALITY_FUNNEL.get(state.get(FACING));
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
		if (context.getEntity() instanceof ItemEntity)
			return AllShapes.REALITY_FUNNEL_COLLISION.get(state.get(FACING));
		return getShape(state, world, pos, context);
	}

	@Override
	public BlockState updatePostPlacement(BlockState state, Direction direction, BlockState p_196271_3_, IWorld world,
		BlockPos pos, BlockPos p_196271_6_) {
		Direction facing = state.get(FACING);
		if (facing.getAxis()
			.isHorizontal()) {
			if (direction == Direction.DOWN) {
				BlockState equivalentFunnel = AllBlocks.BELT_FUNNEL.getDefaultState()
					.with(BeltFunnelBlock.HORIZONTAL_FACING, facing)
					.with(BeltFunnelBlock.POWERED, state.get(POWERED));
				if (BeltFunnelBlock.isOnValidBelt(equivalentFunnel, world, pos))
					return equivalentFunnel;
			}
			if (direction == facing) {
				BlockState equivalentFunnel = AllBlocks.CHUTE_FUNNEL.getDefaultState()
					.with(ChuteFunnelBlock.HORIZONTAL_FACING, facing)
					.with(ChuteFunnelBlock.POWERED, state.get(POWERED));
				if (ChuteFunnelBlock.isOnValidChute(equivalentFunnel, world, pos))
					return equivalentFunnel;
			}
			if (direction == facing.getOpposite()) {
				BlockState equivalentFunnel = AllBlocks.CHUTE_FUNNEL.getDefaultState()
					.with(ChuteFunnelBlock.HORIZONTAL_FACING, facing.getOpposite())
					.cycle(ChuteFunnelBlock.PUSHING)
					.with(ChuteFunnelBlock.POWERED, state.get(POWERED));
				if (ChuteFunnelBlock.isOnValidChute(equivalentFunnel, world, pos))
					return equivalentFunnel;
			}
		}
		return state;
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (worldIn.isRemote)
			return;
		boolean previouslyPowered = state.get(POWERED);
		if (previouslyPowered != worldIn.isBlockPowered(pos))
			worldIn.setBlockState(pos, state.cycle(POWERED), 2);
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
		Block block = world.getBlockState(pos.offset(state.get(FACING)
			.getOpposite()))
			.getBlock();
		return !(block instanceof RealityFunnelBlock) && !(block instanceof HorizontalInteractionFunnelBlock);
	}

	@Nullable
	public static Direction getFunnelFacing(BlockState state) {
		if (state.has(FACING))
			return state.get(FACING);
		if (state.has(BlockStateProperties.HORIZONTAL_FACING))
			return state.get(BlockStateProperties.HORIZONTAL_FACING);
		return null;
	}

	@Override
	public void onReplaced(BlockState p_196243_1_, World p_196243_2_, BlockPos p_196243_3_, BlockState p_196243_4_,
		boolean p_196243_5_) {
		if (p_196243_1_.hasTileEntity() && (p_196243_1_.getBlock() != p_196243_4_.getBlock() && !isFunnel(p_196243_4_)
			|| !p_196243_4_.hasTileEntity())) {
			p_196243_2_.removeTileEntity(p_196243_3_);
		}
	}

	@Nullable
	public static boolean isFunnel(BlockState state) {
		return state.getBlock() instanceof RealityFunnelBlock
			|| state.getBlock() instanceof HorizontalInteractionFunnelBlock;
	}

	@Override
	public Class<RealityFunnelTileEntity> getTileEntityClass() {
		return RealityFunnelTileEntity.class;
	}

}
