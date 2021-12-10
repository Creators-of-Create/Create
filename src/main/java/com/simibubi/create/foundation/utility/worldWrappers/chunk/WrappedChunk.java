package com.simibubi.create.foundation.utility.worldWrappers.chunk;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.TickContainerAccess;

public class WrappedChunk extends ChunkAccess {

	final PlacementSimulationWorld world;
	boolean needsLight;
	final int x;
	final int z;

	private final LevelChunkSection[] sections;

	public WrappedChunk(PlacementSimulationWorld world, int x, int z) {
		super(new ChunkPos(x, z), UpgradeData.EMPTY, world, world.registryAccess()
			.registry(Registry.BIOME_REGISTRY)
			.orElseThrow(), 0L, null, null);

		this.world = world;
		this.needsLight = true;
		this.x = x;
		this.z = z;

		this.sections = new LevelChunkSection[16];

		for (int i = 0; i < 16; i++) {
			sections[i] = new WrappedChunkSection(this, i << 4);
		}
	}

	@Override
	public Stream<BlockPos> getLights() {
		return world.blocksAdded.entrySet()
			.stream()
			.filter(it -> {
				BlockPos blockPos = it.getKey();
				boolean chunkContains = blockPos.getX() >> 4 == x && blockPos.getZ() >> 4 == z;
				return chunkContains && it.getValue()
					.getLightEmission(/*world, blockPos*/) != 0;
			})
			.map(Map.Entry::getKey);
	}

	@Override
	public LevelChunkSection[] getSections() {
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
	public void setBlockEntity(BlockEntity p_177426_2_) {}

	@Override
	public void addEntity(Entity p_76612_1_) {}

	@Override
	public Set<BlockPos> getBlockEntitiesPos() {
		return null;
	}

	@Override
	public Collection<Map.Entry<Heightmap.Types, Heightmap>> getHeightmaps() {
		return null;
	}

	@Override
	public void setHeightmap(Heightmap.Types p_201607_1_, long[] p_201607_2_) {}

	@Override
	public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types p_217303_1_) {
		return null;
	}

	@Override
	public int getHeight(Heightmap.Types p_201576_1_, int p_201576_2_, int p_201576_3_) {
		return 0;
	}

	@Override
	public void setUnsaved(boolean p_177427_1_) {}

	@Override
	public boolean isUnsaved() {
		return false;
	}

	@Override
	public void removeBlockEntity(BlockPos p_177425_1_) {}

	@Override
	public ShortList[] getPostProcessing() {
		return new ShortList[0];
	}

	@Nullable
	@Override
	public CompoundTag getBlockEntityNbt(BlockPos p_201579_1_) {
		return null;
	}

	@Nullable
	@Override
	public CompoundTag getBlockEntityNbtForSaving(BlockPos p_223134_1_) {
		return null;
	}

	@Override
	public UpgradeData getUpgradeData() {
		return null;
	}

	@Override
	public void setInhabitedTime(long p_177415_1_) {}

	@Override
	public long getInhabitedTime() {
		return 0;
	}

	@Override
	public boolean isLightCorrect() {
		return needsLight;
	}

	@Override
	public void setLightCorrect(boolean needsLight) {
		this.needsLight = needsLight;
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos pos) {
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
	public void addReferenceForFeature(StructureFeature<?> arg0, long arg1) {}

	@Override
	public Map<StructureFeature<?>, LongSet> getAllReferences() {
		return null;
	}

	@Override
	public LongSet getReferencesForFeature(StructureFeature<?> arg0) {
		return null;
	}

	@Override
	public StructureStart<?> getStartForFeature(StructureFeature<?> arg0) {
		return null;
	}

	@Override
	public void setAllReferences(Map<StructureFeature<?>, LongSet> arg0) {}

	@Override
	public void setStartForFeature(StructureFeature<?> arg0, StructureStart<?> arg1) {}

	@Override
	public void setAllStarts(Map<StructureFeature<?>, StructureStart<?>> p_201612_1_) {}

	@Override
	public Map<StructureFeature<?>, StructureStart<?>> getAllStarts() {
		return null;
	}

	@Override
	public int getHeight() {
		return world.getHeight();
	}

	@Override
	public int getMinBuildHeight() {
		return world.getMinBuildHeight();
	}

	@Override
	public TickContainerAccess<Fluid> getFluidTicks() {
		return BlackholeTickAccess.emptyContainer();
	}

	@Override
	public TicksToSave getTicksForSerialization() {
		return null;
	}

	@Override
	public TickContainerAccess<Block> getBlockTicks() {
		return BlackholeTickAccess.emptyContainer();
	}

}
