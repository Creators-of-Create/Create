package com.simibubi.create.content.contraptions.components.structureMovement.interaction;

import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;

import net.minecraft.block.BlockState;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;

public class TrapdoorMovingInteraction extends SimpleBlockMovingInteraction {

	@Override
	protected BlockState handle(PlayerEntity player, Contraption contraption, BlockPos pos, BlockState currentState) {
		SoundEvent sound = currentState.getValue(TrapDoorBlock.OPEN) ? SoundEvents.WOODEN_TRAPDOOR_CLOSE
			: SoundEvents.WOODEN_TRAPDOOR_OPEN;
		float pitch = player.level.random.nextFloat() * 0.1F + 0.9F;
		playSound(player, sound, pitch);
		return currentState.cycle(TrapDoorBlock.OPEN);
	}

	@Override
	protected boolean updateColliders() {
		return true;
	}

}
