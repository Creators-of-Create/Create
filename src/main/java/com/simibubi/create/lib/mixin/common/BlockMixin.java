package com.simibubi.create.lib.mixin.common;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.simibubi.create.lib.extensions.BlockExtensions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(Block.class)
public abstract class BlockMixin extends BlockBehaviour implements BlockExtensions {
	private BlockMixin(Properties properties) {
		super(properties);
	}

	@Shadow
	public abstract SoundType getSoundType(BlockState blockState);

	@Override
	public SoundType create$getSoundType(BlockState state, LevelReader world, BlockPos pos, @Nullable Entity entity) {
		return getSoundType(state);
	}

	@Override
	public int create$getLightValue(BlockState state, BlockGetter world, BlockPos pos) {
		return state.getLightEmission();
	}
}
