package com.simibubi.create.foundation.utility.worldWrappers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.utility.worldWrappers.chunk.EmptierChunk;
import com.simibubi.create.foundation.utility.worldWrappers.chunk.WrappedChunk;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.biome.BiomeRegistry;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.server.ChunkHolder;

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

    public IChunk getChunk(int x, int z) {
        long pos = ChunkPos.asLong(x, z);

        if (chunks == null)
        	return new EmptierChunk();

        return chunks.computeIfAbsent(pos, $ -> new WrappedChunk(world, x, z));
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
