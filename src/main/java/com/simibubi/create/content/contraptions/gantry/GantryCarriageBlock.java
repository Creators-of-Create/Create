package com.simibubi.create.content.contraptions.gantry;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.gantry.GantryShaftBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class GantryCarriageBlock extends DirectionalAxisKineticBlock implements IBE<GantryCarriageBlockEntity> {

	public GantryCarriageBlock(Properties properties) {
		super(properties);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		Direction direction = state.getValue(FACING);
		BlockState shaft = world.getBlockState(pos.relative(direction.getOpposite()));
		return AllBlocks.GANTRY_SHAFT.has(shaft) && shaft.getValue(GantryShaftBlock.FACING)
			.getAxis() != direction.getAxis();
	}

	@Override
	public void updateIndirectNeighbourShapes(BlockState stateIn, LevelAccessor worldIn, BlockPos pos, int flags, int count) {
		super.updateIndirectNeighbourShapes(stateIn, worldIn, pos, flags, count);
		withBlockEntityDo(worldIn, pos, GantryCarriageBlockEntity::checkValidGantryShaft);
	}

	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, worldIn, pos, oldState, isMoving);
	}

	@Override
	protected Direction getFacingForPlacement(BlockPlaceContext context) {
		return context.getClickedFace();
	}

	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
		BlockHitResult hit) {
		if (!player.mayBuild() || player.isShiftKeyDown())
			return InteractionResult.PASS;
		if (player.getItemInHand(handIn)
			.isEmpty()) {
			withBlockEntityDo(worldIn, pos, be -> be.checkValidGantryShaft());
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState stateForPlacement = super.getStateForPlacement(context);
		Direction opposite = stateForPlacement.getValue(FACING)
			.getOpposite();
		return cycleAxisIfNecessary(stateForPlacement, opposite, context.getLevel()
			.getBlockState(context.getClickedPos()
				.relative(opposite)));
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block p_220069_4_, BlockPos updatePos,
		boolean p_220069_6_) {
		if (updatePos.equals(pos.relative(state.getValue(FACING)
			.getOpposite())) && !canSurvive(state, world, pos))
			world.destroyBlock(pos, true);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState otherState, LevelAccessor world,
		BlockPos pos, BlockPos p_196271_6_) {
		if (state.getValue(FACING) != direction.getOpposite())
			return state;
		return cycleAxisIfNecessary(state, direction, otherState);
	}

	protected BlockState cycleAxisIfNecessary(BlockState state, Direction direction, BlockState otherState) {
		if (!AllBlocks.GANTRY_SHAFT.has(otherState))
			return state;
		if (otherState.getValue(GantryShaftBlock.FACING)
			.getAxis() == direction.getAxis())
			return state;
		if (isValidGantryShaftAxis(state, otherState))
			return state;
		return state.cycle(AXIS_ALONG_FIRST_COORDINATE);
	}

	public static boolean isValidGantryShaftAxis(BlockState pinionState, BlockState gantryState) {
		return getValidGantryShaftAxis(pinionState) == gantryState.getValue(GantryShaftBlock.FACING)
			.getAxis();
	}

	public static Axis getValidGantryShaftAxis(BlockState state) {
		if (!(state.getBlock() instanceof GantryCarriageBlock))
			return Axis.Y;
		IRotate block = (IRotate) state.getBlock();
		Axis rotationAxis = block.getRotationAxis(state);
		Axis facingAxis = state.getValue(FACING)
			.getAxis();
		for (Axis axis : Iterate.axes)
			if (axis != rotationAxis && axis != facingAxis)
				return axis;
		return Axis.Y;
	}

	public static Axis getValidGantryPinionAxis(BlockState state, Axis shaftAxis) {
		Axis facingAxis = state.getValue(FACING)
			.getAxis();
		for (Axis axis : Iterate.axes)
			if (axis != shaftAxis && axis != facingAxis)
				return axis;
		return Axis.Y;
	}

	@Override
	public Class<GantryCarriageBlockEntity> getBlockEntityClass() {
		return GantryCarriageBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends GantryCarriageBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.GANTRY_PINION.get();
	}
	
}
