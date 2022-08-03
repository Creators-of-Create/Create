package com.simibubi.create.foundation.utility.worldWrappers;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;

public class WrappedWorld extends Level {

	protected Level world;
	protected ChunkSource chunkSource;

	protected LevelEntityGetter<Entity> entityGetter = new DummyLevelEntityGetter<>();

	public WrappedWorld(Level world) {
		super((WritableLevelData) world.getLevelData(), world.dimension(), world.dimensionTypeRegistration(),
			world::getProfiler, world.isClientSide, world.isDebug(), 0, 0);
		this.world = world;
	}

	public void setChunkSource(ChunkSource source) {
		this.chunkSource = source;
	}

	public Level getLevel() {
		return world;
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return world.getLightEngine();
	}

	@Override
	public BlockState getBlockState(@Nullable BlockPos pos) {
		return world.getBlockState(pos);
	}

	@Override
	public boolean isStateAtPosition(BlockPos p_217375_1_, Predicate<BlockState> p_217375_2_) {
		return world.isStateAtPosition(p_217375_1_, p_217375_2_);
	}

	@Override
	@Nullable
	public BlockEntity getBlockEntity(BlockPos pos) {
		return world.getBlockEntity(pos);
	}

	@Override
	public boolean setBlock(BlockPos pos, BlockState newState, int flags) {
		return world.setBlock(pos, newState, flags);
	}

	@Override
	public int getMaxLocalRawBrightness(BlockPos pos) {
		return 15;
	}

	@Override
	public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
		world.sendBlockUpdated(pos, oldState, newState, flags);
	}

	@Override
	public LevelTickAccess<Block> getBlockTicks() {
		return world.getBlockTicks();
	}

	@Override
	public LevelTickAccess<Fluid> getFluidTicks() {
		return world.getFluidTicks();
	}

	@Override
	public ChunkSource getChunkSource() {
		return chunkSource != null ? chunkSource : world.getChunkSource();
	}

	@Override
	public void levelEvent(@Nullable Player player, int type, BlockPos pos, int data) {}

	@Override
	public List<? extends Player> players() {
		return Collections.emptyList();
	}

	@Override
	public void playSeededSound(Player p_220363_, double p_220364_, double p_220365_, double p_220366_,
			SoundEvent p_220367_, SoundSource p_220368_, float p_220369_, float p_220370_, long p_220371_) {}

	@Override
	public void playSeededSound(Player p_220372_, Entity p_220373_, SoundEvent p_220374_, SoundSource p_220375_,
			float p_220376_, float p_220377_, long p_220378_) {}

	@Override
	public void playSound(@Nullable Player player, double x, double y, double z, SoundEvent soundIn,
		SoundSource category, float volume, float pitch) {}

	@Override
	public void playSound(@Nullable Player p_217384_1_, Entity p_217384_2_, SoundEvent p_217384_3_,
		SoundSource p_217384_4_, float p_217384_5_, float p_217384_6_) {}

	@Override
	public Entity getEntity(int id) {
		return null;
	}

	@Override
	public MapItemSavedData getMapData(String mapName) {
		return null;
	}

	@Override
	public boolean addFreshEntity(Entity entityIn) {
		entityIn.level = world;
		return world.addFreshEntity(entityIn);
	}

	@Override
	public void setMapData(String pMapId, MapItemSavedData pData) {}

	@Override
	public int getFreeMapId() {
		return world.getFreeMapId();
	}

	@Override
	public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {}

	@Override
	public Scoreboard getScoreboard() {
		return world.getScoreboard();
	}

	@Override
	public RecipeManager getRecipeManager() {
		return world.getRecipeManager();
	}

	@Override
	public Holder<Biome> getUncachedNoiseBiome(int p_225604_1_, int p_225604_2_, int p_225604_3_) {
		return world.getUncachedNoiseBiome(p_225604_1_, p_225604_2_, p_225604_3_);
	}

	@Override
	public RegistryAccess registryAccess() {
		return world.registryAccess();
	}

	@Override
	public float getShade(Direction p_230487_1_, boolean p_230487_2_) {
		return world.getShade(p_230487_1_, p_230487_2_);
	}

	@Override
	public void updateNeighbourForOutputSignal(BlockPos p_175666_1_, Block p_175666_2_) {}

	@Override
	public void gameEvent(Entity pEntity, GameEvent pEvent, BlockPos pPos) {}

	@Override
	public void gameEvent(GameEvent p_220404_, Vec3 p_220405_, Context p_220406_) {}

	@Override
	public String gatherChunkSourceStats() {
		return world.gatherChunkSourceStats();
	}

	@Override
	protected LevelEntityGetter<Entity> getEntities() {
		return entityGetter;
	}

	// Intentionally copied from LevelHeightAccessor. Workaround for issues caused
	// when other mods (such as Lithium)
	// override the vanilla implementations in ways which cause WrappedWorlds to
	// return incorrect, default height info.
	// WrappedWorld subclasses should implement their own getMinBuildHeight and
	// getHeight overrides where they deviate
	// from the defaults for their dimension.

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
