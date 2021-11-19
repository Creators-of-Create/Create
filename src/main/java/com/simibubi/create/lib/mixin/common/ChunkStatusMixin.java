package com.simibubi.create.lib.mixin.common;

import net.minecraft.world.level.chunk.ChunkStatus;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkStatus.class)
public class ChunkStatusMixin {
	@Shadow
	@Final
	@Mutable
	public static ChunkStatus FULL;
}
