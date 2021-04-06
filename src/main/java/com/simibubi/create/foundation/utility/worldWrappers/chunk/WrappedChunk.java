package com.simibubi.create.foundation.utility.worldWrappers.chunk;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import ca.spottedleaf.starlight.common.chunk.ExtendedChunk;
import ca.spottedleaf.starlight.common.light.SWMRNibbleArray;
import ca.spottedleaf.starlight.common.light.StarLightEngine;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.world.ITickList;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;

public class WrappedChunk implements IChunk {

    final PlacementSimulationWorld world;
    boolean needsLight;
    final int x;
    final int z;
    final ChunkPos pos;

    private final ChunkSection[] sections;

    public WrappedChunk(PlacementSimulationWorld world, int x, int z) {
        this.world = world;
        this.needsLight = true;
        this.x = x;
        this.z = z;
        this.pos = new ChunkPos(x, z);

        this.sections = new ChunkSection[16];

        for (int i = 0; i < 16; i++) {
            sections[i] = new WrappedChunkSection(this, i << 4);
        }
    }

    @Override
    public Stream<BlockPos> getLightSources() {
        return world.blocksAdded
                .entrySet()
                .stream()
                .filter(it -> {
                    BlockPos blockPos = it.getKey();
                    boolean chunkContains = blockPos.getX() >> 4 == x && blockPos.getZ() >> 4 == z;
                    return chunkContains && it.getValue().getLightValue(world, blockPos) != 0;
                })
                .map(Map.Entry::getKey);
    }

    @Override
    public ChunkSection[] getSections() {
        return sections;
    }

    @Override
    public ChunkStatus getStatus() {
        return ChunkStatus.LIGHT;
    }

    @Nullable
    @Override
    public BlockState setBlockState(BlockPos p_177436_1_, BlockState p_177436_2_, boolean p_177436_3_) {
        return null;
    }

    @Override
    public void addTileEntity(BlockPos p_177426_1_, TileEntity p_177426_2_) {

    }

    @Override
    public void addEntity(Entity p_76612_1_) {

    }

    @Override
    public Set<BlockPos> getTileEntitiesPos() {
        return null;
    }

    @Override
    public Collection<Map.Entry<Heightmap.Type, Heightmap>> func_217311_f() {
        return null;
    }

    @Override
    public void setHeightmap(Heightmap.Type p_201607_1_, long[] p_201607_2_) {

    }

    @Override
    public Heightmap getHeightmap(Heightmap.Type p_217303_1_) {
        return null;
    }

    @Override
    public int getTopBlockY(Heightmap.Type p_201576_1_, int p_201576_2_, int p_201576_3_) {
        return 0;
    }

    @Override
    public ChunkPos getPos() {
        return pos;
    }

    @Override
    public void setLastSaveTime(long p_177432_1_) {

    }

    @Nullable
    @Override
    public BiomeContainer getBiomeArray() {
        return null;
    }

    @Override
    public void setModified(boolean p_177427_1_) {

    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void removeTileEntity(BlockPos p_177425_1_) {

    }

    @Override
    public ShortList[] getPackedPositions() {
        return new ShortList[0];
    }

    @Nullable
    @Override
    public CompoundNBT getDeferredTileEntity(BlockPos p_201579_1_) {
        return null;
    }

    @Nullable
    @Override
    public CompoundNBT func_223134_j(BlockPos p_223134_1_) {
        return null;
    }

    @Override
    public ITickList<Block> getBlocksToBeTicked() {
        return null;
    }

    @Override
    public ITickList<Fluid> getFluidsToBeTicked() {
        return null;
    }

    @Override
    public UpgradeData getUpgradeData() {
        return null;
    }

    @Override
    public void setInhabitedTime(long p_177415_1_) {

    }

    @Override
    public long getInhabitedTime() {
        return 0;
    }

    @Override
    public boolean hasLight() {
        return needsLight;
    }

    @Override
    public void setLight(boolean needsLight) {
        this.needsLight = needsLight;
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return world.getBlockState(pos);
    }

	@Override
	public FluidState getFluidState(BlockPos p_204610_1_) {
		return null;
	}

	@Override
	public void addStructureReference(Structure<?> arg0, long arg1) {
		
	}

	@Override
	public Map<Structure<?>, LongSet> getStructureReferences() {
		return null;
	}

	@Override
	public LongSet getStructureReferences(Structure<?> arg0) {
		return null;
	}

	@Override
	public StructureStart<?> getStructureStart(Structure<?> arg0) {
		return null;
	}

	@Override
	public void setStructureReferences(Map<Structure<?>, LongSet> arg0) {
		
	}

	@Override
	public void setStructureStart(Structure<?> arg0, StructureStart<?> arg1) {
		
	}

	@Override
	public void setStructureStarts(Map<Structure<?>, StructureStart<?>> p_201612_1_) {
		
	}

	@Override
	public Map<Structure<?>, StructureStart<?>> getStructureStarts() {
		return null;
	}


}
