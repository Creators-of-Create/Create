package com.simibubi.create.content.logistics.block.realityFunnel;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public abstract class HorizontalInteractionFunnelBlock extends HorizontalBlock implements IWrenchable {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final BooleanProperty PUSHING = BooleanProperty.create("pushing");
	private BlockEntry<? extends RealityFunnelBlock> parent;

	public HorizontalInteractionFunnelBlock(BlockEntry<? extends RealityFunnelBlock> parent, Properties p_i48377_1_) {
		super(p_i48377_1_);
		this.parent = parent;
		BlockState defaultState = getDefaultState().with(PUSHING, true);
		if (hasPoweredProperty())
			defaultState = defaultState.with(POWERED, false);
		setDefaultState(defaultState);
	}

	public abstract boolean hasPoweredProperty();

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.REALITY_FUNNEL.create();
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> p_206840_1_) {
		if (hasPoweredProperty())
			p_206840_1_.add(POWERED);
		super.fillStateContainer(p_206840_1_.add(HORIZONTAL_FACING, PUSHING));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext ctx) {
		BlockState stateForPlacement = super.getStateForPlacement(ctx);
		if (hasPoweredProperty())
			stateForPlacement = stateForPlacement.with(POWERED, ctx.getWorld()
				.isBlockPowered(ctx.getPos()));
		return stateForPlacement;
	}

	@Override
	public void onReplaced(BlockState p_196243_1_, World p_196243_2_, BlockPos p_196243_3_, BlockState p_196243_4_,
		boolean p_196243_5_) {
		if (p_196243_1_.hasTileEntity()
			&& (p_196243_1_.getBlock() != p_196243_4_.getBlock() && !RealityFunnelBlock.isFunnel(p_196243_4_)
				|| !p_196243_4_.hasTileEntity())) {
			p_196243_2_.removeTileEntity(p_196243_3_);
		}
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos,
		PlayerEntity player) {
		return parent.asStack();
	}

	@Override
	public BlockState updatePostPlacement(BlockState state, Direction direction, BlockState neighbour, IWorld world,
		BlockPos pos, BlockPos p_196271_6_) {
		if (!canStillInteract(state, world, pos))
			return parent.getDefaultState()
				.with(RealityFunnelBlock.FACING, state.get(HORIZONTAL_FACING));
		return state;
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
		return !world.getBlockState(pos.offset(state.get(HORIZONTAL_FACING)
			.getOpposite()))
			.getShape(world, pos)
			.isEmpty();
	}

	protected abstract boolean canStillInteract(BlockState state, IWorldReader world, BlockPos pos);

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (!hasPoweredProperty())
			return;
		if (worldIn.isRemote)
			return;
		boolean previouslyPowered = state.get(POWERED);
		if (previouslyPowered != worldIn.isBlockPowered(pos))
			worldIn.setBlockState(pos, state.cycle(POWERED), 2);
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		if (!context.getWorld().isRemote)
			context.getWorld()
				.setBlockState(context.getPos(), state.cycle(PUSHING));
		return ActionResultType.SUCCESS;
	}

}
