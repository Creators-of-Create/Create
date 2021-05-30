package com.simibubi.create.foundation.utility.worldWrappers;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.ITickList;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.storage.ISpawnWorldInfo;
import net.minecraft.world.storage.MapData;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WrappedWorld extends World {

	protected World world;
	private AbstractChunkProvider provider;

	public WrappedWorld(World world, AbstractChunkProvider provider) {
		super((ISpawnWorldInfo) world.getWorldInfo(), world.getRegistryKey(), world.getDimension(), world::getProfiler,
				world.isRemote, world.isDebugWorld(), 0);
		this.world = world;
		this.provider = provider;
	}

	public WrappedWorld(World world) {
		this(world, null);
	}

	public World getWorld() {
		return world;
	}

	@Override
	public WorldLightManager getLightingProvider() {
		return world.getLightingProvider();
	}

	@Override
	public BlockState getBlockState(@Nullable BlockPos pos) {
		return world.getBlockState(pos);
	}

	@Override
	public boolean hasBlockState(@Nullable BlockPos p_217375_1_, @Nullable Predicate<BlockState> p_217375_2_) {
		return world.hasBlockState(p_217375_1_, p_217375_2_);
	}

	@Override
	public TileEntity getTileEntity(@Nullable BlockPos pos) {
		return world.getTileEntity(pos);
	}

	@Override
	public boolean setBlockState(@Nullable BlockPos pos, @Nullable BlockState newState, int flags) {
		return world.setBlockState(pos, newState, flags);
	}

	@Override
	public int getLight(BlockPos pos) {
		return 15;
	}

	@Override
	public void notifyBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
		Chunk chunk = world.getChunkProvider().getWorldChunk(pos.getX() >> 4, pos.getZ() >> 4);
		if (chunk != null) {
			ChunkSection chunksection = chunk.getSections()[SectionPos.toChunk(pos.getY())];
			if (chunksection != null)
				world.notifyBlockUpdate(pos, oldState, newState, flags);
		}
	}

	@Override
	public ITickList<Block> getPendingBlockTicks() {
		return world.getPendingBlockTicks();
	}

	@Override
	public ITickList<Fluid> getPendingFluidTicks() {
		return world.getPendingFluidTicks();
	}

	@Override
	public AbstractChunkProvider getChunkProvider() {
		return provider;
	}

	@Override
	public void playEvent(@Nullable PlayerEntity player, int type, BlockPos pos, int data) {}

	@Override
	public List<? extends PlayerEntity> getPlayers() {
		return Collections.emptyList();
	}

	@Override
	public void playSound(@Nullable PlayerEntity player, double x, double y, double z, SoundEvent soundIn,
		SoundCategory category, float volume, float pitch) {}

	@Override
	public void playMovingSound(@Nullable PlayerEntity p_217384_1_, Entity p_217384_2_, SoundEvent p_217384_3_,
		SoundCategory p_217384_4_, float p_217384_5_, float p_217384_6_) {}

	@Override
	public Entity getEntityByID(int id) {
		return null;
	}

	@Override
	public MapData getMapData(String mapName) {
		return null;
	}

	@Override
	public boolean addEntity(@Nullable Entity entityIn) {
		if (entityIn == null)
			return false;
		entityIn.setWorld(world);
		return world.addEntity(entityIn);
	}

	@Override
	public void registerMapData(MapData mapDataIn) {}

	@Override
	public int getNextMapId() {
		return world.getNextMapId();
	}

	@Override
	public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {}

	@Override
	public Scoreboard getScoreboard() {
		return world.getScoreboard();
	}

	@Override
	public RecipeManager getRecipeManager() {
		return world.getRecipeManager();
	}

	@Override
	public ITagCollectionSupplier getTags() {
		return world.getTags();
	}

	@Override
	public Biome getGeneratorStoredBiome(int p_225604_1_, int p_225604_2_, int p_225604_3_) {
		return world.getGeneratorStoredBiome(p_225604_1_, p_225604_2_, p_225604_3_);
	}

	@Override
	public DynamicRegistries getRegistryManager() {
		return world.getRegistryManager();
	}

	@Override
	public float getBrightness(Direction p_230487_1_, boolean p_230487_2_) {
		return world.getBrightness(p_230487_1_, p_230487_2_);
	}

	@Override
	public void markChunkDirty(BlockPos p_175646_1_, TileEntity p_175646_2_) {
	}

	@Override
	public boolean isBlockLoaded(BlockPos p_175667_1_) {
		return true;
	}

	@Override
	public void updateComparatorOutputLevel(BlockPos p_175666_1_, Block p_175666_2_) {
		return;
	}
}
