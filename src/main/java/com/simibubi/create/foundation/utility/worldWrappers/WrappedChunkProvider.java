package com.simibubi.create.foundation.utility.worldWrappers;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.utility.worldWrappers.chunk.EmptierChunk;
import com.simibubi.create.foundation.utility.worldWrappers.chunk.WrappedChunk;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class WrappedChunkProvider extends ChunkSource {
	private PlacementSimulationWorld world;
	private Level fallbackWorld;

	public HashMap<Long, WrappedChunk> chunks;

	public WrappedChunkProvider setFallbackWorld(Level world) {
		fallbackWorld = world;
		return this;
	}

	public WrappedChunkProvider setPlacementWorld(PlacementSimulationWorld world) {
		fallbackWorld = this.world = world;
		this.chunks = new HashMap<>();
		return this;
	}

	public Stream<BlockPos> getLightSources() {
		return world.blocksAdded.entrySet()
			.stream()
			.filter(it -> it.getValue()
				.getLightEmission(/*world, it.getKey()*/) != 0)
			.map(Map.Entry::getKey);
	}

	@Nullable
	@Override
	public BlockGetter getChunkForLighting(int x, int z) {
		return getChunk(x, z);
	}

	@Override
	public Level getLevel() {
		return fallbackWorld;
	}

	@Nullable
	@Override
	public ChunkAccess getChunk(int x, int z, ChunkStatus status, boolean p_212849_4_) {
		return getChunk(x, z);
	}

	public ChunkAccess getChunk(int x, int z) {
		long pos = ChunkPos.asLong(x, z);

		if (chunks == null)
			return new EmptierChunk(fallbackWorld.registryAccess());

		return chunks.computeIfAbsent(pos, $ -> new WrappedChunk(world, x, z));
	}

	@Override
	public String gatherStats() {
		return "WrappedChunkProvider";
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return world.getLightEngine();
	}

	@Override
	public void tick(BooleanSupplier pHasTimeLeft) {}

	@Override
	public int getLoadedChunksCount() {
		return 0;
	}
}
