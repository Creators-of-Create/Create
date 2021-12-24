package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;

import com.simibubi.create.lib.extensions.BlockStateExtensions;

import net.minecraft.world.level.block.state.BlockState;

@Mixin(BlockState.class)
public abstract class BlockStateMixin implements BlockStateExtensions {
	// This space for rent (DO NOT DELETE THIS MIXIN, THE INTERFACE IMPLEMENTATION IS IMPORTANT)
}
