package com.simibubi.create.foundation.utility.worldWrappers;

import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ITickList;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerTickList;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.SaveFormat;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WrappedServerWorld extends ServerWorld {

	protected World world;

	public WrappedServerWorld(World world) {
		// Replace null with world.getChunkProvider().chunkManager.progressListener ? We had null in 1.15
		super(world.getServer(), Util.backgroundExecutor(), getLevelSaveFromWorld(world), (IServerWorldInfo) world.getLevelData(), world.dimension(), world.dimensionType(), null, ((ServerChunkProvider) world.getChunkSource()).getGenerator(), world.isDebug(), world.getBiomeManager().biomeZoomSeed, Collections.EMPTY_LIST, false); //, world.field_25143);
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
	public ServerTickList<Block> getBlockTicks() {
		ITickList<Block> tl =  world.getBlockTicks();
		if (tl instanceof ServerTickList)
			return (ServerTickList<Block>) tl;
		return super.getBlockTicks();
	}

	@Override
	public ServerTickList<Fluid> getLiquidTicks() {
		ITickList<Fluid> tl =  world.getLiquidTicks();
		if (tl instanceof ServerTickList)
			return (ServerTickList<Fluid>) tl;
		return super.getLiquidTicks();
	}

	@Override
	public void levelEvent(PlayerEntity player, int type, BlockPos pos, int data) {
	}

	@Override
	public List<ServerPlayerEntity> players() {
		return Collections.emptyList();
	}

	@Override
	public void playSound(PlayerEntity player, double x, double y, double z, SoundEvent soundIn, SoundCategory category,
			float volume, float pitch) {
	}

	@Override
	public void playSound(PlayerEntity p_217384_1_, Entity p_217384_2_, SoundEvent p_217384_3_,
			SoundCategory p_217384_4_, float p_217384_5_, float p_217384_6_) {
	}

	@Override
	public Entity getEntity(int id) {
		return null;
	}

	@Override
	public MapData getMapData(String mapName) {
		return null;
	}

	@Override
	public boolean addFreshEntity(Entity entityIn) {
		entityIn.setLevel(world);
		return world.addFreshEntity(entityIn);
	}

	@Override
	public void setMapData(MapData mapDataIn) {
	}

	@Override
	public int getFreeMapId() {
		return 0;
	}

	@Override
	public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {
	}

	@Override
	public RecipeManager getRecipeManager() {
		return world.getRecipeManager();
	}

	@Override
	public ITagCollectionSupplier getTagManager() {
		return world.getTagManager();
	}

	@Override
	public Biome getUncachedNoiseBiome(int p_225604_1_, int p_225604_2_, int p_225604_3_) {
		return world.getUncachedNoiseBiome(p_225604_1_, p_225604_2_, p_225604_3_);
	}

	private static SaveFormat.LevelSave getLevelSaveFromWorld(World world) {
		return ObfuscationReflectionHelper.getPrivateValue(MinecraftServer.class, world.getServer(), "field_71310_m"); // storageSource
	}
}
