package com.simibubi.create.foundation.utility.worldWrappers;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.scores.Scoreboard;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WrappedWorld extends Level {

	protected Level world;
	protected ChunkSource provider;

	public WrappedWorld(Level world, ChunkSource provider) {
		super((WritableLevelData) world.getLevelData(), world.dimension(), world.dimensionType(), world::getProfiler,
				world.isClientSide, world.isDebug(), 0);
		this.world = world;
		this.provider = provider;
	}

	public WrappedWorld(Level world) {
		this(world, null);
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
	public boolean isStateAtPosition(@Nullable BlockPos p_217375_1_, @Nullable Predicate<BlockState> p_217375_2_) {
		return world.isStateAtPosition(p_217375_1_, p_217375_2_);
	}

	@Override
	public BlockEntity getBlockEntity(@Nullable BlockPos pos) {
		return world.getBlockEntity(pos);
	}

	@Override
	public boolean setBlock(@Nullable BlockPos pos, @Nullable BlockState newState, int flags) {
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
	public TickList<Block> getBlockTicks() {
		return world.getBlockTicks();
	}

	@Override
	public TickList<Fluid> getLiquidTicks() {
		return world.getLiquidTicks();
	}

	@Override
	public ChunkSource getChunkSource() {
		return provider;
	}

	@Override
	public void levelEvent(@Nullable Player player, int type, BlockPos pos, int data) {}

	@Override
	public List<? extends Player> players() {
		return Collections.emptyList();
	}

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
	public boolean addFreshEntity(@Nullable Entity entityIn) {
		if (entityIn == null)
			return false;
		entityIn.setLevel(world);
		return world.addFreshEntity(entityIn);
	}

	@Override
	public void setMapData(MapItemSavedData mapDataIn) {}

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
	public TagContainer getTagManager() {
		return world.getTagManager();
	}

	@Override
	public Biome getUncachedNoiseBiome(int p_225604_1_, int p_225604_2_, int p_225604_3_) {
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
	public void blockEntityChanged(BlockPos p_175646_1_, BlockEntity p_175646_2_) {
	}

	@Override
	public boolean hasChunkAt(BlockPos p_175667_1_) {
		return true;
	}

	@Override
	public void updateNeighbourForOutputSignal(BlockPos p_175666_1_, Block p_175666_2_) {
		return;
	}
}
