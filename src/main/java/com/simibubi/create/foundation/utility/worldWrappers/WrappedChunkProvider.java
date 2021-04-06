package com.simibubi.create.foundation.utility.worldWrappers;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.utility.worldWrappers.chunk.WrappedChunk;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.lighting.WorldLightManager;

public class WrappedChunkProvider extends AbstractChunkProvider {
    private PlacementSimulationWorld world;

    public HashMap<Long, WrappedChunk> chunks;

    protected final ChunkFactory chunkFactory;

    public WrappedChunkProvider(ChunkFactory chunkFactory) {
        this.chunkFactory = chunkFactory;
    }

    public WrappedChunkProvider setWorld(PlacementSimulationWorld world) {
        this.world = world;
        this.chunks = new HashMap<>();
        return this;
    }

    public Stream<BlockPos> getLightSources() {
        return world.blocksAdded
                .entrySet()
                .stream()
                .filter(it -> it.getValue().getLightValue(world, it.getKey()) != 0)
                .map(Map.Entry::getKey);
    }

    @Nullable
    @Override
    public IBlockReader getChunkForLight(int x, int z) {
        return getChunk(x, z);
    }

    @Override
    public IBlockReader getWorld() {
        return world;
    }

    @Nullable
    @Override
    public IChunk getChunk(int x, int z, ChunkStatus status, boolean p_212849_4_) {
        return getChunk(x, z);
    }

    public WrappedChunk getChunk(int x, int z) {
        long pos = ChunkPos.asLong(x, z);
        
        if (chunks == null)
        	return null;


        return chunks.computeIfAbsent(pos, $ -> chunkFactory.create(world, x, z));
    }

    @Override
    public String makeString() {
        return "WrappedChunkProvider";
    }

    @Override
    public WorldLightManager getLightManager() {
        return world.getLightingProvider();
    }

    public interface ChunkFactory {
        WrappedChunk create(PlacementSimulationWorld world, int x, int z);
    }
}
