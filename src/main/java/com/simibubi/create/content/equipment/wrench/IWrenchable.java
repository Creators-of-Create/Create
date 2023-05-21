package com.simibubi.create.content.equipment.wrench;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.utility.VoxelShaper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface IWrenchable {

	default InteractionResult onWrenched(BlockState state, UseOnContext context) {
		Level world = context.getLevel();
		BlockState rotated = getRotatedBlockState(state, context.getClickedFace());
		if (!rotated.canSurvive(world, context.getClickedPos()))
			return InteractionResult.PASS;

		KineticBlockEntity.switchToBlockState(world, context.getClickedPos(), updateAfterWrenched(rotated, context));

		BlockEntity be = context.getLevel()
			.getBlockEntity(context.getClickedPos());
		if (be instanceof GeneratingKineticBlockEntity) {
			((GeneratingKineticBlockEntity) be).reActivateSource = true;
		}

		if (world.getBlockState(context.getClickedPos()) != state)
			playRotateSound(world, context.getClickedPos());

		return InteractionResult.SUCCESS;
	}

	default BlockState updateAfterWrenched(BlockState newState, UseOnContext context) {
//		return newState;
		return Block.updateFromNeighbourShapes(newState, context.getLevel(), context.getClickedPos());
	}

	default InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Player player = context.getPlayer();
		if (world instanceof ServerLevel) {
			if (player != null && !player.isCreative())
				Block.getDrops(state, (ServerLevel) world, pos, world.getBlockEntity(pos), player, context.getItemInHand())
					.forEach(itemStack -> {
						player.getInventory().placeItemBackInInventory(itemStack);
					});
			state.spawnAfterBreak((ServerLevel) world, pos, ItemStack.EMPTY);
			world.destroyBlock(pos, false);
			playRemoveSound(world, pos);
		}
		return InteractionResult.SUCCESS;
	}

	default void playRemoveSound(Level world, BlockPos pos) {
		AllSoundEvents.WRENCH_REMOVE.playOnServer(world, pos, 1, Create.RANDOM.nextFloat() * .5f + .5f);
	}

	default void playRotateSound(Level world, BlockPos pos) {
		AllSoundEvents.WRENCH_ROTATE.playOnServer(world, pos, 1, Create.RANDOM.nextFloat() + .5f);
	}

	default BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
		BlockState newState = originalState;

		if (targetedFace.getAxis() == Direction.Axis.Y) {
			if (originalState.hasProperty(HorizontalAxisKineticBlock.HORIZONTAL_AXIS))
				return originalState.setValue(HorizontalAxisKineticBlock.HORIZONTAL_AXIS, VoxelShaper
					.axisAsFace(originalState.getValue(HorizontalAxisKineticBlock.HORIZONTAL_AXIS))
					.getClockWise(targetedFace.getAxis())
					.getAxis());
			if (originalState.hasProperty(HorizontalKineticBlock.HORIZONTAL_FACING))
				return originalState.setValue(HorizontalKineticBlock.HORIZONTAL_FACING, originalState
					.getValue(HorizontalKineticBlock.HORIZONTAL_FACING).getClockWise(targetedFace.getAxis()));
		}

		if (originalState.hasProperty(RotatedPillarKineticBlock.AXIS))
			return originalState.setValue(RotatedPillarKineticBlock.AXIS,
				VoxelShaper
					.axisAsFace(originalState.getValue(RotatedPillarKineticBlock.AXIS))
					.getClockWise(targetedFace.getAxis())
					.getAxis());

		if (!originalState.hasProperty(DirectionalKineticBlock.FACING))
			return originalState;

		Direction stateFacing = originalState.getValue(DirectionalKineticBlock.FACING);

		if (stateFacing.getAxis()
			.equals(targetedFace.getAxis())) {
			if (originalState.hasProperty(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE))
				return originalState.cycle(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE);
			else
				return originalState;
		} else {
			do {
				newState = newState.setValue(DirectionalKineticBlock.FACING,
					newState.getValue(DirectionalKineticBlock.FACING).getClockWise(targetedFace.getAxis()));
				if (targetedFace.getAxis() == Direction.Axis.Y
					&& newState.hasProperty(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE))
					newState = newState.cycle(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE);
			} while (newState.getValue(DirectionalKineticBlock.FACING)
				.getAxis()
				.equals(targetedFace.getAxis()));
		}
		return newState;
	}
}
