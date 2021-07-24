package com.simibubi.create.content.contraptions.wrench;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.contraptions.base.DirectionalKineticBlock;
import com.simibubi.create.content.contraptions.base.GeneratingKineticTileEntity;
import com.simibubi.create.content.contraptions.base.HorizontalAxisKineticBlock;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.utility.DirectionHelper;
import com.simibubi.create.foundation.utility.VoxelShaper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public interface IWrenchable {

	default ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		World world = context.getLevel();
		BlockState rotated = getRotatedBlockState(state, context.getClickedFace());
		if (!rotated.canSurvive(world, context.getClickedPos()))
			return ActionResultType.PASS;

		KineticTileEntity.switchToBlockState(world, context.getClickedPos(), updateAfterWrenched(rotated, context));

		TileEntity te = context.getLevel()
			.getBlockEntity(context.getClickedPos());
		if (te != null)
			te.clearCache();
		if (te instanceof GeneratingKineticTileEntity) {
			((GeneratingKineticTileEntity) te).reActivateSource = true;
		}

		if (world.getBlockState(context.getClickedPos()) != state)
			playRotateSound(world, context.getClickedPos());

		return ActionResultType.SUCCESS;
	}

	default BlockState updateAfterWrenched(BlockState newState, ItemUseContext context) {
//		return newState;
		return Block.updateFromNeighbourShapes(newState, context.getLevel(), context.getClickedPos());
	}

	default ActionResultType onSneakWrenched(BlockState state, ItemUseContext context) {
		World world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		PlayerEntity player = context.getPlayer();
		if (world instanceof ServerWorld) {
			if (player != null && !player.isCreative())
				Block.getDrops(state, (ServerWorld) world, pos, world.getBlockEntity(pos), player, context.getItemInHand())
					.forEach(itemStack -> {
						player.inventory.placeItemBackInInventory(world, itemStack);
					});
			state.spawnAfterBreak((ServerWorld) world, pos, ItemStack.EMPTY);
			world.destroyBlock(pos, false);
			playRemoveSound(world, pos);
		}
		return ActionResultType.SUCCESS;
	}

	default void playRemoveSound(World world, BlockPos pos) {
		AllSoundEvents.WRENCH_REMOVE.playOnServer(world, pos, 1, Create.RANDOM.nextFloat() * .5f + .5f);
	}

	default void playRotateSound(World world, BlockPos pos) {
		AllSoundEvents.WRENCH_ROTATE.playOnServer(world, pos, 1, Create.RANDOM.nextFloat() + .5f);
	}

	default BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
		BlockState newState = originalState;

		if (targetedFace.getAxis() == Direction.Axis.Y) {
			if (originalState.hasProperty(HorizontalAxisKineticBlock.HORIZONTAL_AXIS))
				return originalState.setValue(HorizontalAxisKineticBlock.HORIZONTAL_AXIS, DirectionHelper
					.rotateAround(VoxelShaper.axisAsFace(originalState.getValue(HorizontalAxisKineticBlock.HORIZONTAL_AXIS)),
						targetedFace.getAxis())
					.getAxis());
			if (originalState.hasProperty(HorizontalKineticBlock.HORIZONTAL_FACING))
				return originalState.setValue(HorizontalKineticBlock.HORIZONTAL_FACING, DirectionHelper
					.rotateAround(originalState.getValue(HorizontalKineticBlock.HORIZONTAL_FACING), targetedFace.getAxis()));
		}

		if (originalState.hasProperty(RotatedPillarKineticBlock.AXIS))
			return originalState.setValue(RotatedPillarKineticBlock.AXIS,
				DirectionHelper
					.rotateAround(VoxelShaper.axisAsFace(originalState.getValue(RotatedPillarKineticBlock.AXIS)),
						targetedFace.getAxis())
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
					DirectionHelper.rotateAround(newState.getValue(DirectionalKineticBlock.FACING), targetedFace.getAxis()));
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
