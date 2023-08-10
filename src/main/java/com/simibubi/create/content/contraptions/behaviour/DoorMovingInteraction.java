package com.simibubi.create.content.contraptions.behaviour;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.decoration.slidingDoor.SlidingDoorBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public class DoorMovingInteraction extends SimpleBlockMovingInteraction {

	@Override
	protected BlockState handle(Player player, Contraption contraption, BlockPos pos, BlockState currentState) {
		if (!(currentState.getBlock() instanceof DoorBlock))
			return currentState;

		boolean trainDoor = currentState.getBlock() instanceof SlidingDoorBlock;
		SoundEvent sound = currentState.getValue(DoorBlock.OPEN) ? trainDoor ? null : SoundEvents.WOODEN_DOOR_CLOSE
			: trainDoor ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.WOODEN_DOOR_OPEN;

		BlockPos otherPos = currentState.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
		StructureBlockInfo info = contraption.getBlocks()
			.get(otherPos);
		if (info.state.hasProperty(DoorBlock.OPEN)) {
			BlockState newState = info.state.cycle(DoorBlock.OPEN);
			setContraptionBlockData(contraption.entity, otherPos, new StructureBlockInfo(info.pos, newState, info.nbt));
		}

		currentState = currentState.cycle(DoorBlock.OPEN);
		
		if (player != null) {

			if (trainDoor) {
				DoorHingeSide hinge = currentState.getValue(SlidingDoorBlock.HINGE);
				Direction facing = currentState.getValue(SlidingDoorBlock.FACING);
				BlockPos doublePos =
					pos.relative(hinge == DoorHingeSide.LEFT ? facing.getClockWise() : facing.getCounterClockWise());
				StructureBlockInfo doubleInfo = contraption.getBlocks()
					.get(doublePos);
				if (doubleInfo != null && SlidingDoorBlock.isDoubleDoor(currentState, hinge, facing, doubleInfo.state))
					handlePlayerInteraction(null, InteractionHand.MAIN_HAND, doublePos, contraption.entity);
			}

			float pitch = player.level.random.nextFloat() * 0.1F + 0.9F;
			if (sound != null)
				playSound(player, sound, pitch);
		}

		return currentState;
	}

	@Override
	protected boolean updateColliders() {
		return true;
	}

}
