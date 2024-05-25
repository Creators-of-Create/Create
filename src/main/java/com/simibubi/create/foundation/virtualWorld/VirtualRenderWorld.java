package com.simibubi.create.foundation.virtualWorld;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import dev.engine_room.flywheel.api.visualization.VisualizationLevel;

import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;

public class VirtualRenderWorld extends Level implements VisualizationLevel {
	protected final Level level;
	protected final int minBuildHeight;
	protected final int height;
	protected final Vec3i biomeOffset;

	protected final VirtualChunkSource chunkSource;
	protected final LevelLightEngine lightEngine;

	protected final Map<BlockPos, BlockState> blockStates = new HashMap<>();
	protected final Map<BlockPos, BlockEntity> blockEntities = new HashMap<>();
	protected final Object2ShortMap<SectionPos> nonEmptyBlockCounts = new Object2ShortOpenHashMap<>();

	protected final LevelEntityGetter<Entity> entityGetter = new VirtualLevelEntityGetter<>();

	protected final BlockPos.MutableBlockPos scratchPos = new BlockPos.MutableBlockPos();

	public VirtualRenderWorld(Level level) {
		this(level, Vec3i.ZERO);
	}

	public VirtualRenderWorld(Level level, Vec3i biomeOffset) {
		this(level, level.getMinBuildHeight(), level.getHeight(), biomeOffset);
	}

	public VirtualRenderWorld(Level level, int minBuildHeight, int height, Vec3i biomeOffset) {
		super((WritableLevelData) level.getLevelData(), level.dimension(), level.registryAccess(), level.dimensionTypeRegistration(), level.getProfilerSupplier(),
				true, false, 0, 0);
		this.level = level;
		this.minBuildHeight = nextMultipleOf16(minBuildHeight);
		this.height = nextMultipleOf16(height);
		this.biomeOffset = biomeOffset;

		this.chunkSource = new VirtualChunkSource(this);
		this.lightEngine = new LevelLightEngine(chunkSource, true, false);
	}

	/**
	 * We need to ensure that height and minBuildHeight are multiples of 16.
	 * Adapted from: https://math.stackexchange.com/questions/291468
	 */
	public static int nextMultipleOf16(int a) {
		if (a < 0) {
			return -(((Math.abs(a) - 1) | 15) + 1);
		} else {
			return ((a - 1) | 15) + 1;
		}
	}

	public void clear() {
		blockStates.clear();
		blockEntities.clear();

		nonEmptyBlockCounts.forEach((sectionPos, nonEmptyBlockCount) -> {
			if (nonEmptyBlockCount > 0) {
				lightEngine.updateSectionStatus(sectionPos, true);
			}
		});

		nonEmptyBlockCounts.clear();

		runLightEngine();
	}

	public void setBlockEntities(Collection<BlockEntity> blockEntities) {
		this.blockEntities.clear();
		blockEntities.forEach(this::setBlockEntity);
	}

	/**
	 * Run this after you're done using setBlock().
	 */
	public void runLightEngine() {
		Set<ChunkPos> chunkPosSet = new ObjectOpenHashSet<>();
		nonEmptyBlockCounts.object2ShortEntrySet().forEach(entry -> {
			if (entry.getShortValue() > 0) {
				chunkPosSet.add(entry.getKey().chunk());
			}
		});
		for (ChunkPos chunkPos : chunkPosSet) {
			lightEngine.propagateLightSources(chunkPos);
		}

		lightEngine.runLightUpdates();
	}

	// MEANINGFUL OVERRIDES

	@Override
	public LevelChunk getChunk(int x, int z) {
		throw new UnsupportedOperationException();
	}

	public ChunkAccess actuallyGetChunk(int x, int z) {
		return getChunk(x, z, ChunkStatus.FULL);
	}

	@Override
	public ChunkAccess getChunk(BlockPos pos) {
		return actuallyGetChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
	}

	@Override
	public boolean setBlock(BlockPos pos, BlockState newState, int flags, int recursionLeft) {
		if (isOutsideBuildHeight(pos)) {
			return false;
		}

		pos = pos.immutable();

		BlockState oldState = getBlockState(pos);
		if (oldState == newState) {
			return false;
		}

		blockStates.put(pos, newState);

		SectionPos sectionPos = SectionPos.of(pos);
		short nonEmptyBlockCount = nonEmptyBlockCounts.getShort(sectionPos);
		boolean prevEmpty = nonEmptyBlockCount == 0;
		if (!oldState.isAir()) {
			--nonEmptyBlockCount;
		}
		if (!newState.isAir()) {
			++nonEmptyBlockCount;
		}
		nonEmptyBlockCounts.put(sectionPos, nonEmptyBlockCount);
		boolean nowEmpty = nonEmptyBlockCount == 0;

		if (prevEmpty != nowEmpty) {
			lightEngine.updateSectionStatus(sectionPos, nowEmpty);
		}

		lightEngine.checkBlock(pos);

		return true;
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return lightEngine;
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		if (isOutsideBuildHeight(pos)) {
			return Blocks.VOID_AIR.defaultBlockState();
		}
		BlockState state = blockStates.get(pos);
		if (state != null) {
			return state;
		}
		return Blocks.AIR.defaultBlockState();
	}

	public BlockState getBlockState(int x, int y, int z) {
		return getBlockState(scratchPos.set(x, y, z));
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		if (isOutsideBuildHeight(pos)) {
			return Fluids.EMPTY.defaultFluidState();
		}
		return getBlockState(pos).getFluidState();
	}

	@Override
	@Nullable
	public BlockEntity getBlockEntity(BlockPos pos) {
		if (!isOutsideBuildHeight(pos)) {
			return blockEntities.get(pos);
		}
		return null;
	}

	@Override
	public void setBlockEntity(BlockEntity blockEntity) {
		BlockPos pos = blockEntity.getBlockPos();
		if (!isOutsideBuildHeight(pos)) {
			blockEntities.put(pos, blockEntity);
		}
	}

	@Override
	public void removeBlockEntity(BlockPos pos) {
		if (!isOutsideBuildHeight(pos)) {
			blockEntities.remove(pos);
		}
	}

	@Override
	protected LevelEntityGetter<Entity> getEntities() {
		return entityGetter;
	}

	@Override
	public ChunkSource getChunkSource() {
		return chunkSource;
	}

	@Override
	public int getMinBuildHeight() {
		return minBuildHeight;
	}

	@Override
	public int getHeight() {
		return height;
	}

	// BIOME OFFSET

	@Override
	public Holder<Biome> getBiome(BlockPos pos) {
		return super.getBiome(pos.offset(biomeOffset));
	}

	@Override
	public Holder<Biome> getNoiseBiome(int x, int y, int z) {
		// Control flow should never reach this method,
		// so we add biomeOffset in case some other mod calls this directly.
		return level.getNoiseBiome(x + biomeOffset.getX(), y + biomeOffset.getY(), z + biomeOffset.getZ());
	}

	@Override
	public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
		// Control flow should never reach this method,
		// so we add biomeOffset in case some other mod calls this directly.
		return level.getUncachedNoiseBiome(x + biomeOffset.getX(), y + biomeOffset.getY(), z + biomeOffset.getZ());
	}

	// RENDERING CONSTANTS

	@Override
	public int getMaxLocalRawBrightness(BlockPos pos) {
		return 15;
	}

	@Override
	public float getShade(Direction direction, boolean shade) {
		return 1f;
	}

	// THIN WRAPPERS

	@Override
	public Scoreboard getScoreboard() {
		return level.getScoreboard();
	}

	@Override
	public RecipeManager getRecipeManager() {
		return level.getRecipeManager();
	}

	@Override
	public BiomeManager getBiomeManager() {
		return level.getBiomeManager();
	}

	@Override
	public LevelTickAccess<Block> getBlockTicks() {
		return level.getBlockTicks();
	}

	@Override
	public LevelTickAccess<Fluid> getFluidTicks() {
		return level.getFluidTicks();
	}

	@Override
	public FeatureFlagSet enabledFeatures() {
		return level.enabledFeatures();
	}

	// ADDITIONAL OVERRRIDES

	@Override
	public void updateNeighbourForOutputSignal(BlockPos pos, Block block) {
	}

	@Override
	public boolean isLoaded(BlockPos pos) {
		return true;
	}

	@Override
	public boolean isAreaLoaded(BlockPos center, int range) {
		return true;
	}

	// UNIMPORTANT IMPLEMENTATIONS

	@Override
	public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
	}

	@Override
	public void playSeededSound(Player player, double x, double y, double z, Holder<SoundEvent> soundEvent,
			SoundSource soundSource, float volume, float pitch, long seed) {
	}

	@Override
	public void playSeededSound(Player player, Entity entity, Holder<SoundEvent> soundEvent, SoundSource soundSource,
			float volume, float pitch, long seed) {
	}

	@Override
	public String gatherChunkSourceStats() {
		return "";
	}

	@Override
	@Nullable
	public Entity getEntity(int id) {
		return null;
	}

	@Override
	@Nullable
	public MapItemSavedData getMapData(String mapName) {
		return null;
	}

	@Override
	public void setMapData(String mapId, MapItemSavedData data) {
	}

	@Override
	public int getFreeMapId() {
		return 0;
	}

	@Override
	public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {
	}

	@Override
	public void levelEvent(@Nullable Player player, int type, BlockPos pos, int data) {
	}

	@Override
	public void gameEvent(GameEvent event, Vec3 position, Context context) {
	}

	@Override
	public List<? extends Player> players() {
		return Collections.emptyList();
	}

	// Override Starlight's ExtendedWorld interface methods:

	public LevelChunk getChunkAtImmediately(final int chunkX, final int chunkZ) {
		return chunkSource.getChunk(chunkX, chunkZ, false);
	}

	public ChunkAccess getAnyChunkImmediately(final int chunkX, final int chunkZ) {
		return chunkSource.getChunk(chunkX, chunkZ);
	}

	// Intentionally copied from LevelHeightAccessor. Lithium overrides these methods so we need to, too.

	@Override
	public int getMaxBuildHeight() {
		return this.getMinBuildHeight() + this.getHeight();
	}

	@Override
	public int getSectionsCount() {
		return this.getMaxSection() - this.getMinSection();
	}

	@Override
	public int getMinSection() {
		return SectionPos.blockToSectionCoord(this.getMinBuildHeight());
	}

	@Override
	public int getMaxSection() {
		return SectionPos.blockToSectionCoord(this.getMaxBuildHeight() - 1) + 1;
	}

	@Override
	public boolean isOutsideBuildHeight(BlockPos pos) {
		return this.isOutsideBuildHeight(pos.getY());
	}

	@Override
	public boolean isOutsideBuildHeight(int y) {
		return y < this.getMinBuildHeight() || y >= this.getMaxBuildHeight();
	}

	@Override
	public int getSectionIndex(int y) {
		return this.getSectionIndexFromSectionY(SectionPos.blockToSectionCoord(y));
	}

	@Override
	public int getSectionIndexFromSectionY(int sectionY) {
		return sectionY - this.getMinSection();
	}

	@Override
	public int getSectionYFromSectionIndex(int sectionIndex) {
		return sectionIndex + this.getMinSection();
	}
}
