package com.simibubi.create.content.curiosities.girder;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.content.logistics.trains.track.TrackBlock;
import com.simibubi.create.content.logistics.trains.track.TrackBlock.TrackShape;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GirderBlock extends Block implements SimpleWaterloggedBlock, IWrenchable {

	public static final BooleanProperty X = BooleanProperty.create("x");
	public static final BooleanProperty Z = BooleanProperty.create("z");
	public static final BooleanProperty TOP = BooleanProperty.create("top");
	public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");

	public GirderBlock(Properties p_49795_) {
		super(p_49795_);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(TOP, false)
			.setValue(BOTTOM, false)
			.setValue(X, false)
			.setValue(Z, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder.add(X, Z, TOP, BOTTOM, WATERLOGGED));
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor world,
		BlockPos pos, BlockPos neighbourPos) {
		if (state.getValue(WATERLOGGED))
			world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		Axis axis = direction.getAxis();
		Property<Boolean> updateProperty =
			axis == Axis.X ? X : axis == Axis.Z ? Z : direction == Direction.UP ? TOP : BOTTOM;
		state = state.setValue(updateProperty, false);
		for (Direction d : Iterate.directionsInAxis(axis))
			state = updateState(world, pos, state, d);
		return state;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Direction face = context.getClickedFace();
		FluidState ifluidstate = level.getFluidState(pos);
		BlockState state = super.getStateForPlacement(context);
		state = state.setValue(X, face.getAxis() == Axis.X);
		state = state.setValue(Z, face.getAxis() == Axis.Z);

		for (Direction d : Iterate.directions)
			state = updateState(level, pos, state, d);

		return state.setValue(WATERLOGGED, Boolean.valueOf(ifluidstate.getType() == Fluids.WATER));
	}

	public static BlockState updateState(LevelAccessor level, BlockPos pos, BlockState state, Direction d) {
		Axis axis = d.getAxis();
		Property<Boolean> updateProperty = axis == Axis.X ? X : axis == Axis.Z ? Z : d == Direction.UP ? TOP : BOTTOM;
		BlockState sideState = level.getBlockState(pos.relative(d));

		if (axis.isVertical()) {
			if (sideState.getBlock() == state.getBlock() && sideState.getValue(X) == sideState.getValue(Z))
				state = state.setValue(updateProperty, true);
			else if (sideState.hasProperty(WallBlock.UP) && sideState.getValue(WallBlock.UP))
				state = state.setValue(updateProperty, true);
			return state;
		}

		if (sideState.getBlock() == state.getBlock() && sideState.getValue(updateProperty))
			state = state.setValue(updateProperty, true);

		for (Direction d2 : Iterate.directionsInAxis(axis == Axis.X ? Axis.Z : Axis.X)) {
			BlockState above = level.getBlockState(pos.above()
				.relative(d2));
			if (AllBlocks.TRACK.has(above)) {
				TrackShape shape = above.getValue(TrackBlock.SHAPE);
				if (shape == (axis == Axis.X ? TrackShape.XO : TrackShape.ZO))
					state = state.setValue(updateProperty, true);
			}
		}

		return state;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		boolean x = state.getValue(GirderBlock.X);
		boolean z = state.getValue(GirderBlock.Z);
		return x ? z ? AllShapes.GIRDER_CROSS : AllShapes.GIRDER_BEAM.get(Axis.X)
			: z ? AllShapes.GIRDER_BEAM.get(Axis.Z) : AllShapes.EIGHT_VOXEL_POLE.get(Axis.Y);
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

}
