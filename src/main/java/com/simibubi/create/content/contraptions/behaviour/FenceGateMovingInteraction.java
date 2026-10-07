package com.simibubi.create.content.contraptions.behaviour;

import com.simibubi.create.content.contraptions.Contraption;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FenceGateMovingInteraction extends SimpleBlockMovingInteraction {

	@Override
	protected BlockState handle(Player player, Contraption contraption, BlockPos pos, BlockState currentState) {
		FenceGateBlock fenceGateBlock = (FenceGateBlock) currentState.getBlock();
		SoundEvent sound = currentState.getValue(FenceGateBlock.OPEN) ? fenceGateBlock.closeSound
				: fenceGateBlock.openSound;
		float pitch = player.level().random.nextFloat() * 0.1F + 0.9F;
		playSound(player, sound, pitch);
		return currentState.cycle(FenceGateBlock.OPEN);
	}

	@Override
	protected boolean updateColliders() {
		return true;
	}

}
