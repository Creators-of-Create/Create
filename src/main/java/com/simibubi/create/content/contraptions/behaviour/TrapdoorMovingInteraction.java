package com.simibubi.create.content.contraptions.behaviour;

import com.simibubi.create.content.contraptions.Contraption;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;

public class TrapdoorMovingInteraction extends SimpleBlockMovingInteraction {

	private final SoundEvent openSound;
	private final SoundEvent closeSound;

	public TrapdoorMovingInteraction() {
		this(SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundEvents.WOODEN_TRAPDOOR_CLOSE);
	}

	public TrapdoorMovingInteraction(SoundEvent openSound, SoundEvent closeSound) {
		this.openSound = openSound;
		this.closeSound = closeSound;
	}

	@Override
	protected BlockState handle(Player player, Contraption contraption, BlockPos pos, BlockState currentState) {
		SoundEvent sound = currentState.getValue(TrapDoorBlock.OPEN) ? closeSound : openSound;
		float pitch = player.level().random.nextFloat() * 0.1F + 0.9F;
		playSound(player, sound, pitch);
		return currentState.cycle(TrapDoorBlock.OPEN);
	}

	@Override
	protected boolean updateColliders() {
		return true;
	}

}
