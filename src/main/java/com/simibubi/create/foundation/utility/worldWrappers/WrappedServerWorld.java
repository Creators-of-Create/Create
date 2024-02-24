package com.simibubi.create.foundation.utility.worldWrappers;

import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.ticks.LevelTicks;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WrappedServerWorld extends ServerLevel {

	protected ServerLevel world;

	public WrappedServerWorld(ServerLevel world) {
		super(world.getServer(), Util.backgroundExecutor(), world.getServer().storageSource,
			(ServerLevelData) world.getLevelData(), world.dimension(),
			new LevelStem(world.dimensionTypeRegistration(), world.getChunkSource().getGenerator()),
			new DummyStatusListener(), world.isDebug(), world.getBiomeManager().biomeZoomSeed,
			Collections.emptyList(), false);
		this.world = world;
	}

	@Override
	public float getSunAngle(float p_72826_1_) {
		return 0;
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
	public LevelTicks<Block> getBlockTicks() {
		return super.getBlockTicks();
	}

	@Override
	public LevelTicks<Fluid> getFluidTicks() {
		return super.getFluidTicks();
	}

	@Override
	public void levelEvent(Player player, int type, BlockPos pos, int data) {}

	@Override
	public List<ServerPlayer> players() {
		return Collections.emptyList();
	}

	@Override
	public void playSound(Player player, double x, double y, double z, SoundEvent soundIn, SoundSource category,
		float volume, float pitch) {}

	@Override
	public void playSound(Player p_217384_1_, Entity p_217384_2_, SoundEvent p_217384_3_, SoundSource p_217384_4_,
		float p_217384_5_, float p_217384_6_) {}

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
	public void setMapData(String mapId, MapItemSavedData mapDataIn) {}

	@Override
	public int getFreeMapId() {
		return 0;
	}

	@Override
	public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {}

	@Override
	public RecipeManager getRecipeManager() {
		return world.getRecipeManager();
	}

	@Override
	public Holder<Biome> getUncachedNoiseBiome(int p_225604_1_, int p_225604_2_, int p_225604_3_) {
		return world.getUncachedNoiseBiome(p_225604_1_, p_225604_2_, p_225604_3_);
	}

}
