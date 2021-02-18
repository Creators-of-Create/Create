package com.simibubi.create.foundation.utility.worldWrappers;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
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

        WrappedChunk chunk = chunks.get(pos);

        if (chunk == null) {
            chunk = new WrappedChunk(world, x, z);
            chunks.put(pos, chunk);
        }

        return chunk;
    }

    @Override
    public void tick(BooleanSupplier p_217207_1_) {

    }

    @Override
    public String makeString() {
        return "WrappedChunkProvider";
    }

    @Override
    public WorldLightManager getLightManager() {
        return world.getLightingProvider();
    }
}
