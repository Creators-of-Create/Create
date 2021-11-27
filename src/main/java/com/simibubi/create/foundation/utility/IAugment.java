package com.simibubi.create.foundation.utility;

import com.simibubi.create.foundation.sound.Sfx;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Called whenever some block is augmented with another, to create something new. For example:
 * - encasing shaft
 * - bracketing pipe
 * - adding shaft to existing belt
 * and similar
 */
public interface IAugment {

// TODO: use this
//	default void onAugment() {}

	default void playAugmentationSound(Level world, BlockPos pos, BlockState state) {
		SoundType soundtype = state.getSoundType();
		world.playSound(null, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS,
				(soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
	}

	default void playAugmentationSound(Level world, BlockPos pos, BlockState state, float volume, float pitch) {
		world.playSound(null, pos, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS, volume, pitch);
	}

	default void playAugmentationSound(Level world, BlockPos pos, Sfx sfx) {
		sfx.playOnServer(world, pos, 1f, 1f);
	}

	default void playAugmentationSound(Level world, BlockPos pos, SoundEvent event) {
		world.playSound(null, pos, event, SoundSource.BLOCKS, 1f, 1f);
	}
}
