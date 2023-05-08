package com.simibubi.create.content.logistics.block.redstone;

import javax.annotation.Nullable;

import com.simibubi.create.content.contraptions.components.structureMovement.ITransformableBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.StructureTransform;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class DirectedDirectionalBlock extends HorizontalDirectionalBlock implements IWrenchable, ITransformableBlock {

	public static final EnumProperty<AttachFace> TARGET = EnumProperty.create("target", AttachFace.class);

	public DirectedDirectionalBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(TARGET, AttachFace.WALL));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder.add(TARGET, FACING));
	}

	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		for (Direction direction : pContext.getNearestLookingDirections()) {
			BlockState blockstate;
			if (direction.getAxis() == Direction.Axis.Y) {
				blockstate = this.defaultBlockState()
					.setValue(TARGET, direction == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR)
					.setValue(FACING, pContext.getHorizontalDirection());
			} else {
				blockstate = this.defaultBlockState()
					.setValue(TARGET, AttachFace.WALL)
					.setValue(FACING, direction.getOpposite());
			}

			return blockstate;
		}

		return null;
	}

	protected static Direction getTargetDirection(BlockState pState) {
		switch ((AttachFace) pState.getValue(TARGET)) {
		case CEILING:
			return Direction.UP;
		case FLOOR:
			return Direction.DOWN;
		default:
			return pState.getValue(FACING);
		}
	}

	@Override
	public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
		if (targetedFace.getAxis() == Axis.Y)
			return IWrenchable.super.getRotatedBlockState(originalState, targetedFace);

		Direction targetDirection = getTargetDirection(originalState);
		Direction newFacing = targetDirection.getClockWise(targetedFace.getAxis());
		if (targetedFace.getAxisDirection() == AxisDirection.NEGATIVE)
			newFacing = newFacing.getOpposite();

		if (newFacing.getAxis() == Axis.Y)
			return originalState.setValue(TARGET, newFacing == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR);
		return originalState.setValue(TARGET, AttachFace.WALL)
			.setValue(FACING, newFacing);
	}

	@Override
	@SuppressWarnings("deprecation")
	public BlockState transform(BlockState state, StructureTransform transform) {
		if (transform.mirror != null)
			state = mirror(state, transform.mirror);
		if (transform.rotationAxis == Direction.Axis.Y)
			return rotate(state, transform.rotation);

		Direction targetDirection = getTargetDirection(state);
		Direction newFacing = transform.rotateFacing(targetDirection);

		if (newFacing.getAxis() == Axis.Y)
			return state.setValue(TARGET, newFacing == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR);
		return state.setValue(TARGET, AttachFace.WALL)
			.setValue(FACING, newFacing);
	}

}
