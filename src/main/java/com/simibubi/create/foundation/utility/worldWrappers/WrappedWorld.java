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
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.ITickList;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.storage.ISpawnWorldInfo;
import net.minecraft.world.storage.MapData;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WrappedWorld extends World {

	protected World world;
	protected AbstractChunkProvider provider;

	public WrappedWorld(World world, AbstractChunkProvider provider) {
		super((ISpawnWorldInfo) world.getLevelData(), world.dimension(), world.dimensionType(), world::getProfiler,
				world.isClientSide, world.isDebug(), 0);
		this.world = world;
		this.provider = provider;
	}

	public WrappedWorld(World world) {
		this(world, null);
	}

	public World getLevel() {
		return world;
	}

	@Override
	public WorldLightManager getLightEngine() {
		return world.getLightEngine();
	}

	@Override
	public BlockState getBlockState(@Nullable BlockPos pos) {
		return world.getBlockState(pos);
	}

	@Override
	public boolean isStateAtPosition(@Nullable BlockPos pPos, @Nullable Predicate<BlockState> pState) {
		return world.isStateAtPosition(pPos, pState);
	}

	@Override
	public TileEntity getBlockEntity(@Nullable BlockPos pos) {
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
	public ITickList<Block> getBlockTicks() {
		return world.getBlockTicks();
	}

	@Override
	public ITickList<Fluid> getLiquidTicks() {
		return world.getLiquidTicks();
	}

	@Override
	public AbstractChunkProvider getChunkSource() {
		return provider;
	}

	@Override
	public void levelEvent(@Nullable PlayerEntity player, int type, BlockPos pos, int data) {}

	@Override
	public List<? extends PlayerEntity> players() {
		return Collections.emptyList();
	}

	@Override
	public void playSound(@Nullable PlayerEntity player, double x, double y, double z, SoundEvent soundIn,
		SoundCategory category, float volume, float pitch) {}

	@Override
	public void playSound(@Nullable PlayerEntity pPlayerIn, Entity pEntityIn, SoundEvent pEventIn,
		SoundCategory pCategoryIn, float pVolume, float pPitch) {}

	@Override
	public Entity getEntity(int id) {
		return null;
	}

	@Override
	public MapData getMapData(String mapName) {
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
	public void setMapData(MapData mapDataIn) {}

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
	public ITagCollectionSupplier getTagManager() {
		return world.getTagManager();
	}

	@Override
	public Biome getUncachedNoiseBiome(int pX, int pY, int pZ) {
		return world.getUncachedNoiseBiome(pX, pY, pZ);
	}

	@Override
	public DynamicRegistries registryAccess() {
		return world.registryAccess();
	}

	@Override
	public float getShade(Direction p_230487_1_, boolean p_230487_2_) {
		return world.getShade(p_230487_1_, p_230487_2_);
	}

	@Override
	public void blockEntityChanged(BlockPos pPos, TileEntity pUnusedTileEntity) {
	}

	@Override
	public boolean hasChunkAt(BlockPos pPos) {
		return true;
	}

	@Override
	public void updateNeighbourForOutputSignal(BlockPos pPos, Block pBlockIn) {
		return;
	}
}
