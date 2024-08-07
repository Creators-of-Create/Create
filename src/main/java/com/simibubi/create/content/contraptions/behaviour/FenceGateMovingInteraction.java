package com.simibubi.create.content.contraptions.behaviour;

import com.simibubi.create.content.contraptions.Contraption;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FenceGateMovingInteraction extends SimpleBlockMovingInteraction {

	@Override
	protected BlockState handle(Player player, Contraption contraption, BlockPos pos, BlockState currentState) {
		SoundEvent sound = currentState.getValue(FenceGateBlock.OPEN) ? SoundEvents.FENCE_GATE_CLOSE
				: SoundEvents.FENCE_GATE_OPEN;
		float pitch = player.level.random.nextFloat() * 0.1F + 0.9F;
		playSound(player, sound, pitch);
		return currentState.cycle(FenceGateBlock.OPEN);
	}

	@Override
	protected boolean updateColliders() {
		return true;
	}

}
