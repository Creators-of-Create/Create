package com.simibubi.create.foundation.render;

import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class VirtualChunkSource extends ChunkSource {
	private final VirtualRenderWorld world;
	private final Long2ObjectMap<VirtualChunk> chunks = new Long2ObjectOpenHashMap<>();

	public VirtualChunkSource(VirtualRenderWorld world) {
		this.world = world;
	}

	@Override
	public Level getLevel() {
		return world;
	}

	public ChunkAccess getChunk(int x, int z) {
		long pos = ChunkPos.asLong(x, z);
		return chunks.computeIfAbsent(pos, $ -> new VirtualChunk(world, x, z));
	}

	@Override
	@Nullable
	public LevelChunk getChunk(int x, int z, boolean load) {
		return null;
	}

	@Override
	@Nullable
	public ChunkAccess getChunk(int x, int z, ChunkStatus status, boolean load) {
		return getChunk(x, z);
	}

	@Override
	public void tick(BooleanSupplier hasTimeLeft, boolean tickChunks) {
	}

	@Override
	public String gatherStats() {
		return "VirtualChunkSource";
	}

	@Override
	public int getLoadedChunksCount() {
		return 0;
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return world.getLightEngine();
	}
}
