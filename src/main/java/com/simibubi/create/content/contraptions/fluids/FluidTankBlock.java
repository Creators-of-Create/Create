package com.simibubi.create.content.contraptions.fluids;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ILightReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FluidTankBlock extends Block {

	public static final BooleanProperty TOP = BooleanProperty.create("top");
	public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");

	public FluidTankBlock(Properties p_i48440_1_) {
		super(p_i48440_1_);
		setDefaultState(getDefaultState().with(TOP, true)
			.with(BOTTOM, true));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> p_206840_1_) {
		p_206840_1_.add(TOP, BOTTOM);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext p_196258_1_) {
		World world = p_196258_1_.getWorld();
		BlockPos pos = p_196258_1_.getPos();
		BlockState state = super.getStateForPlacement(p_196258_1_);
		state = updateState(state, world, pos, Direction.UP);
		state = updateState(state, world, pos, Direction.DOWN);
		return state;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		boolean top = state.get(TOP);
		boolean bottom = state.get(BOTTOM);
		return top ? bottom ? AllShapes.TANK_TOP_BOTTOM : AllShapes.TANK_TOP
			: bottom ? AllShapes.TANK_BOTTOM : AllShapes.TANK;
	}

	@Override
	public BlockState updatePostPlacement(BlockState state, Direction direction, BlockState p_196271_3_, IWorld world,
		BlockPos pos, BlockPos p_196271_6_) {
		return updateState(state, world, pos, direction);
	}

	private BlockState updateState(BlockState state, ILightReader reader, BlockPos pos, Direction direction) {
		if (direction.getAxis()
			.isHorizontal())
			return state;
		return state.with(direction == Direction.UP ? TOP : BOTTOM,
			!AllBlocks.FLUID_TANK.has(reader.getBlockState(pos.offset(direction))));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
		return adjacentBlockState.getBlock() == this;
	}

	public static boolean shouldDrawDiagonalFiller(ILightReader world, BlockPos pos, BlockState state, boolean north,
		boolean east) {
		if (!isTank(state))
			return false;
		int northOffset = north ? 1 : -1;
		int eastOffset = east ? 1 : -1;
		if (!isTank(world.getBlockState(pos.north(northOffset))))
			return false;
		if (!isTank(world.getBlockState(pos.east(eastOffset))))
			return false;
		if (isTank(world.getBlockState(pos.east(eastOffset)
			.north(northOffset))))
			return false;
		return true;
	}

	public static boolean shouldDrawCapFiller(ILightReader world, BlockPos pos, BlockState state, Direction direction,
		boolean top) {
		if (!isTank(state))
			return false;
		if (top && !state.get(TOP))
			return false;
		if (!top && !state.get(BOTTOM))
			return false;
		BlockPos adjacentPos = pos.offset(direction);
		BlockState adjacentState = world.getBlockState(adjacentPos);
		if (!isTank(adjacentState))
			return false;
		if (top && adjacentState.get(TOP))
			return false;
		if (!top && adjacentState.get(BOTTOM))
			return false;
		return true;
	}

	public static boolean isTank(BlockState state) {
		return state.getBlock() instanceof FluidTankBlock;
	}

}
