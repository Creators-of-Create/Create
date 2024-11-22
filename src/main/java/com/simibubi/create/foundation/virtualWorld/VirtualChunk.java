package com.simibubi.create.foundation.virtualWorld;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.TickContainerAccess;

public class VirtualChunk extends ChunkAccess {
	public final VirtualRenderWorld world;

	private final VirtualChunkSection[] sections;

	private boolean needsLight;

	public VirtualChunk(VirtualRenderWorld world, int x, int z) {
		super(new ChunkPos(x, z), UpgradeData.EMPTY, world, world.registryAccess()
			.registryOrThrow(Registries.BIOME), 0L, null, null);

		this.world = world;

		int sectionCount = world.getSectionsCount();
		this.sections = new VirtualChunkSection[sectionCount];

		for (int i = 0; i < sectionCount; i++) {
			sections[i] = new VirtualChunkSection(this, i << 4);
		}

		this.needsLight = true;

//		Mods.STARLIGHT.executeIfInstalled(() -> () -> {
//			((ExtendedChunk) this).setBlockNibbles(StarLightEngine.getFilledEmptyLight(this));
//			((ExtendedChunk) this).setSkyNibbles(StarLightEngine.getFilledEmptyLight(this));
//		});
	}

	@Override
	@Nullable
	public BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
		return null;
	}

	@Override
	public void setBlockEntity(BlockEntity blockEntity) {
	}

	@Override
	public void addEntity(Entity entity) {
	}

	@Override
	public Set<BlockPos> getBlockEntitiesPos() {
		return Collections.emptySet();
	}

	@Override
	public LevelChunkSection[] getSections() {
		return sections;
	}

	@Override
	public Collection<Map.Entry<Heightmap.Types, Heightmap>> getHeightmaps() {
		return Collections.emptySet();
	}

	@Override
	public void setHeightmap(Heightmap.Types type, long[] data) {
	}

	@Override
	public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types type) {
		return null;
	}

	@Override
	public int getHeight(Heightmap.Types type, int x, int z) {
		return 0;
	}

	@Override
	@Nullable
	public StructureStart getStartForStructure(Structure structure) {
		return null;
	}

	@Override
	public void setStartForStructure(Structure structure, StructureStart structureStart) {
	}

	@Override
	public Map<Structure, StructureStart> getAllStarts() {
		return Collections.emptyMap();
	}

	@Override
	public void setAllStarts(Map<Structure, StructureStart> structureStarts) {
	}

	@Override
	public LongSet getReferencesForStructure(Structure pStructure) {
		return LongSets.emptySet();
	}

	@Override
	public void addReferenceForStructure(Structure structure, long reference) {
	}

	@Override
	public Map<Structure, LongSet> getAllReferences() {
		return Collections.emptyMap();
	}

	@Override
	public void setAllReferences(Map<Structure, LongSet> structureReferencesMap) {
	}

	@Override
	public void setUnsaved(boolean unsaved) {
	}

	@Override
	public boolean isUnsaved() {
		return false;
	}

	@Override
	public ChunkStatus getStatus() {
		return ChunkStatus.LIGHT;
	}

	@Override
	public void removeBlockEntity(BlockPos pos) {
	}

	@Override
	public ShortList[] getPostProcessing() {
		return new ShortList[0];
	}

	@Override
	@Nullable
	public CompoundTag getBlockEntityNbt(BlockPos pos) {
		return null;
	}

	@Override
	@Nullable
	public CompoundTag getBlockEntityNbtForSaving(BlockPos pos) {
		return null;
	}

	@Override
	public void findBlocks(BiPredicate<BlockState, BlockPos> predicate, BiConsumer<BlockPos, BlockState> consumer) {
		world.blockStates.forEach((pos, state) -> {
			if (SectionPos.blockToSectionCoord(pos.getX()) == chunkPos.x && SectionPos.blockToSectionCoord(pos.getZ()) == chunkPos.z) {
				if (predicate.test(state, pos)) {
					consumer.accept(pos, state);
				}
			}
		});
	}

	@Override
	public TickContainerAccess<Block> getBlockTicks() {
		return BlackholeTickAccess.emptyContainer();
	}

	@Override
	public TickContainerAccess<Fluid> getFluidTicks() {
		return BlackholeTickAccess.emptyContainer();
	}

	@Override
	public TicksToSave getTicksForSerialization() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getInhabitedTime() {
		return 0;
	}

	@Override
	public void setInhabitedTime(long amount) {
	}

	@Override
	public boolean isLightCorrect() {
		return needsLight;
	}

	@Override
	public void setLightCorrect(boolean lightCorrect) {
		this.needsLight = lightCorrect;
	}

	@Override
	@Nullable
	public BlockEntity getBlockEntity(BlockPos pos) {
		return world.getBlockEntity(pos);
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return world.getBlockState(pos);
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return world.getFluidState(pos);
	}
}
