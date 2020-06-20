package com.simibubi.create.content.logistics.block.realityFunnel;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock.Slope;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelBlock;
import com.simibubi.create.content.logistics.block.depot.DepotBlock;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VoxelShaper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ILightReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class BeltFunnelBlock extends HorizontalBlock implements IWrenchable {

	public static final IProperty<Shape> SHAPE = EnumProperty.create("shape", Shape.class);

	public enum Shape implements IStringSerializable {
		RETRACTED(AllShapes.BELT_FUNNEL_RETRACTED),
		DEFAULT(AllShapes.BELT_FUNNEL_DEFAULT),
		EXTENDED(AllShapes.BELT_FUNNEL_EXTENDED);

		VoxelShaper shaper;

		private Shape(VoxelShaper shaper) {
			this.shaper = shaper;
		}

		@Override
		public String getName() {
			return Lang.asId(name());
		}
	}

	public BeltFunnelBlock(Properties p_i48377_1_) {
		super(p_i48377_1_);
		setDefaultState(getDefaultState().with(SHAPE, Shape.DEFAULT));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> p_206840_1_) {
		super.fillStateContainer(p_206840_1_.add(HORIZONTAL_FACING, SHAPE));
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		return state.get(SHAPE).shaper.get(state.get(HORIZONTAL_FACING));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext p_196258_1_) {
		return updateShape(super.getStateForPlacement(p_196258_1_), p_196258_1_.getWorld(), p_196258_1_.getPos());
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos,
		PlayerEntity player) {
		return AllBlocks.REALITY_FUNNEL.asStack();
	}
	
	@Override
	public BlockState updatePostPlacement(BlockState state, Direction direction, BlockState neighbour, IWorld world,
		BlockPos pos, BlockPos p_196271_6_) {
		if (direction == Direction.DOWN && !isOnValidBelt(state, world, pos))
			return AllBlocks.REALITY_FUNNEL.getDefaultState()
				.with(RealityFunnelBlock.FACING, state.get(HORIZONTAL_FACING));
		if (direction == state.get(HORIZONTAL_FACING))
			return updateShape(state, world, pos);
		return state;
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
		return !world.getBlockState(pos.offset(state.get(HORIZONTAL_FACING)
			.getOpposite()))
			.getShape(world, pos)
			.isEmpty();
	}

	public static boolean isOnValidBelt(BlockState state, IWorldReader world, BlockPos pos) {
		BlockState stateBelow = world.getBlockState(pos.down());
		if (stateBelow.getBlock() instanceof DepotBlock)
			return true;
		if (!(stateBelow.getBlock() instanceof BeltBlock))
			return false;
		if (stateBelow.get(BeltBlock.SLOPE) == Slope.VERTICAL)
			return false;
		if (stateBelow.get(BeltBlock.HORIZONTAL_FACING)
			.getAxis() != state.get(HORIZONTAL_FACING)
				.getAxis())
			return false;
		return true;
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (worldIn.isRemote)
			return;

		Direction blockFacing = state.get(HORIZONTAL_FACING)
			.getOpposite();
		if (fromPos.equals(pos.offset(blockFacing)))
			if (!isValidPosition(state, worldIn, pos))
				worldIn.destroyBlock(pos, true);
	}

	private BlockState updateShape(BlockState state, ILightReader world, BlockPos pos) {
		state = state.with(SHAPE, Shape.DEFAULT);
		BlockState neighbour = world.getBlockState(pos.offset(state.get(HORIZONTAL_FACING)));
		if (canConnectTo(state, neighbour))
			return state.with(SHAPE, Shape.EXTENDED);
		return state;
	}

	private boolean canConnectTo(BlockState state, BlockState neighbour) {
		if (neighbour.getBlock() instanceof BeltTunnelBlock)
			return true;
		if (neighbour.getBlock() == this && neighbour.get(HORIZONTAL_FACING) == state.get(HORIZONTAL_FACING)
			.getOpposite())
			return true;
		return false;
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		if (!context.getWorld().isRemote)
			context.getWorld()
				.setBlockState(context.getPos(), state.cycle(SHAPE));
		return ActionResultType.SUCCESS;
	}

}
