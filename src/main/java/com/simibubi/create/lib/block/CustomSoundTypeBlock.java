package com.simibubi.create.lib.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public interface CustomSoundTypeBlock {
	SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, Entity entity);
}
