package com.simibubi.create.lib.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;

@Mixin(ChunkMap.class)
public interface ChunkMapAccessor {
	@Accessor("updatingChunkMap")
	Long2ObjectLinkedOpenHashMap<ChunkHolder> create$loadedChunks();

	@Accessor("pendingUnloads")
	Long2ObjectLinkedOpenHashMap<ChunkHolder> create$chunksToUnload();

	@Accessor("modified")
	void create$immutableLoadedChunksDirty(boolean v);

	@Invoker("scheduleUnload")
	void create$scheduleUnload(long l, ChunkHolder chunkHolder);
}
