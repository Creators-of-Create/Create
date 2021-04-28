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
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public interface IWrenchable {

	default ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		World world = context.getWorld();
		BlockState rotated = getRotatedBlockState(state, context.getFace());
		if (!rotated.isValidPosition(world, context.getPos()))
			return ActionResultType.PASS;

		KineticTileEntity.switchToBlockState(world, context.getPos(), updateAfterWrenched(rotated, context));

		TileEntity te = context.getWorld()
			.getTileEntity(context.getPos());
		if (te != null)
			te.updateContainingBlockInfo();
		if (te instanceof GeneratingKineticTileEntity) {
			((GeneratingKineticTileEntity) te).reActivateSource = true;
		}

		if (world.getBlockState(context.getPos()) != state)
			playRotateSound(world, context.getPos());

		return ActionResultType.SUCCESS;
	}

	default BlockState updateAfterWrenched(BlockState newState, ItemUseContext context) {
//		return newState;
		return Block.getValidBlockForPosition(newState, context.getWorld(), context.getPos());
	}

	default ActionResultType onSneakWrenched(BlockState state, ItemUseContext context) {
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		PlayerEntity player = context.getPlayer();
		if (world instanceof ServerWorld) {
			if (player != null && !player.isCreative())
				Block.getDrops(state, (ServerWorld) world, pos, world.getTileEntity(pos), player, context.getItem())
					.forEach(itemStack -> {
						player.inventory.placeItemBackInInventory(world, itemStack);
					});
			state.spawnAdditionalDrops((ServerWorld) world, pos, ItemStack.EMPTY);
			world.destroyBlock(pos, false);
			playRemoveSound(world, pos);
		}
		return ActionResultType.SUCCESS;
	}

	default void playRemoveSound(World world, BlockPos pos) {
		AllSoundEvents.WRENCH_REMOVE.playOnServer(world, pos, 1, Create.random.nextFloat() * .5f + .5f);
	}
	
	default void playRotateSound(World world, BlockPos pos) {
		AllSoundEvents.WRENCH_ROTATE.playOnServer(world, pos, 1, Create.random.nextFloat() + .5f);
	}

	default BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
		BlockState newState = originalState;

		if (targetedFace.getAxis() == Direction.Axis.Y) {
			if (originalState.contains(HorizontalAxisKineticBlock.HORIZONTAL_AXIS))
				return originalState.with(HorizontalAxisKineticBlock.HORIZONTAL_AXIS, DirectionHelper
					.rotateAround(VoxelShaper.axisAsFace(originalState.get(HorizontalAxisKineticBlock.HORIZONTAL_AXIS)),
						targetedFace.getAxis())
					.getAxis());
			if (originalState.contains(HorizontalKineticBlock.HORIZONTAL_FACING))
				return originalState.with(HorizontalKineticBlock.HORIZONTAL_FACING, DirectionHelper
					.rotateAround(originalState.get(HorizontalKineticBlock.HORIZONTAL_FACING), targetedFace.getAxis()));
		}

		if (originalState.contains(RotatedPillarKineticBlock.AXIS))
			return originalState.with(RotatedPillarKineticBlock.AXIS,
				DirectionHelper
					.rotateAround(VoxelShaper.axisAsFace(originalState.get(RotatedPillarKineticBlock.AXIS)),
						targetedFace.getAxis())
					.getAxis());

		if (!originalState.contains(DirectionalKineticBlock.FACING))
			return originalState;

		Direction stateFacing = originalState.get(DirectionalKineticBlock.FACING);

		if (stateFacing.getAxis()
			.equals(targetedFace.getAxis())) {
			if (originalState.contains(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE))
				return originalState.cycle(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE);
			else
				return originalState;
		} else {
			do {
				newState = newState.with(DirectionalKineticBlock.FACING,
					DirectionHelper.rotateAround(newState.get(DirectionalKineticBlock.FACING), targetedFace.getAxis()));
				if (targetedFace.getAxis() == Direction.Axis.Y
					&& newState.contains(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE))
					newState = newState.cycle(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE);
			} while (newState.get(DirectionalKineticBlock.FACING)
				.getAxis()
				.equals(targetedFace.getAxis()));
		}
		return newState;
	}
}
