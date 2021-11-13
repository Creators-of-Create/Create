package com.simibubi.create.lib.mixin.accessor;

import java.util.EnumSet;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;

@Mixin(ChunkStatus.class)
public interface ChunkStatusAccessor {
	@Accessor("FULL")
	static void create$setFull(ChunkStatus chunkStatus) {
		throw new RuntimeException("mixin applying went wrong");
	}

	@Invoker("<init>")
	static ChunkStatus newChunkStatus(String string, @Nullable ChunkStatus chunkStatus, int i, EnumSet<Heightmap.Types> enumSet, ChunkStatus.ChunkType type, ChunkStatus.GenerationTask iGenerationWorker, ChunkStatus.LoadingTask iLoadingWorker) {
		throw new RuntimeException("mixin applying went wrong");
	}
}
