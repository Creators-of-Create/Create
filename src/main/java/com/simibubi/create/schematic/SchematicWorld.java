package com.simibubi.create.schematic;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.particles.IParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EmptyTickList;
import net.minecraft.world.ITickList;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.storage.WorldInfo;

public class SchematicWorld implements IWorld {

	private Map<BlockPos, BlockState> blocks;
	private Cuboid bounds;
	private BlockPos anchor;
	
	public SchematicWorld(Map<BlockPos, BlockState> blocks, Cuboid bounds, BlockPos anchor) {
		this.blocks = blocks;
		this.setBounds(bounds);
		this.anchor = anchor;
		updateBlockstates();
	}
	
	private void updateBlockstates() {
		Set<BlockPos> keySet = new HashSet<>(blocks.keySet());
		keySet.forEach(pos -> {
			BlockState blockState = blocks.get(pos);
			if (blockState == null)
				return;
			blockState.updateNeighbors(this, pos.add(anchor), 16);
		});
	}

	public Set<BlockPos> getAllPositions() {
		return blocks.keySet();
	}
	
	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return null;
	}

	@Override
	public BlockState getBlockState(BlockPos globalPos) {
		BlockPos pos = globalPos.subtract(anchor);
		if (getBounds().contains(pos) && blocks.containsKey(pos)) {
			return blocks.get(pos);
		} else {
			return Blocks.AIR.getDefaultState();
		}
	}

	public Map<BlockPos, BlockState> getBlockMap() {
		return blocks;
	}

	@Override
	public IFluidState getFluidState(BlockPos pos) {
		return new FluidState(Fluids.EMPTY, ImmutableMap.of());
	}

	@Override
	public Biome getBiome(BlockPos pos) {
		return Biomes.THE_VOID;
	}

	@Override
	public int getLightFor(LightType type, BlockPos pos) {
		return 10;
	}

	@Override
	public List<Entity> getEntitiesInAABBexcluding(Entity arg0, AxisAlignedBB arg1, Predicate<? super Entity> arg2) {
		return Collections.emptyList();
	}

	@Override
	public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> arg0, AxisAlignedBB arg1,
			Predicate<? super T> arg2) {
		return Collections.emptyList();
	}

	@Override
	public List<? extends PlayerEntity> getPlayers() {
		return Collections.emptyList();
	}

	@Override
	public int getLightSubtracted(BlockPos pos, int amount) {
		return 0;
	}

	@Override
	public IChunk getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull) {
		return null;
	}

	@Override
	public BlockPos getHeight(Type heightmapType, BlockPos pos) {
		return BlockPos.ZERO;
	}

	@Override
	public int getHeight(Type heightmapType, int x, int z) {
		return 0;
	}

	@Override
	public int getSkylightSubtracted() {
		return 0;
	}

	@Override
	public WorldBorder getWorldBorder() {
		return null;
	}

	@Override
	public boolean isRemote() {
		return false;
	}

	@Override
	public int getSeaLevel() {
		return 0;
	}

	@Override
	public Dimension getDimension() {
		return null;
	}

	@Override
	public boolean hasBlockState(BlockPos pos, Predicate<BlockState> predicate) {
		return predicate.test(getBlockState(pos));
	}

	@Override
	public boolean destroyBlock(BlockPos arg0, boolean arg1) {
		return setBlockState(arg0, Blocks.AIR.getDefaultState(), 3);
	}

	@Override
	public boolean removeBlock(BlockPos arg0, boolean arg1) {
		return setBlockState(arg0, Blocks.AIR.getDefaultState(), 3);
	}

	@Override
	public boolean setBlockState(BlockPos pos, BlockState arg1, int arg2) {
		pos = pos.subtract(anchor);
		if (pos.getX() < bounds.x) {
			bounds.width += bounds.x - pos.getX();
			bounds.x = pos.getX();
		}
		if (pos.getY() < bounds.y) {
			bounds.height += bounds.y - pos.getY();
			bounds.y = pos.getY();
		}
		if (pos.getZ() < bounds.z) {
			bounds.length += bounds.z - pos.getZ();
			bounds.z = pos.getZ();
		}
		BlockPos boundsMax = bounds.getOrigin().add(bounds.getSize());
		if (boundsMax.getX() <= pos.getX()) {
			bounds.width += pos.getX() - boundsMax.getX() + 1;
		}
		if (boundsMax.getY() <= pos.getY()) {
			bounds.height += pos.getY() - boundsMax.getY() + 1;
		}
		if (boundsMax.getZ() <= pos.getZ()) {
			bounds.length += pos.getZ() - boundsMax.getZ() + 1;
		}
		
		blocks.put(pos, arg1);
		return true;
	}

	@Override
	public long getSeed() {
		return 0;
	}

	@Override
	public ITickList<Block> getPendingBlockTicks() {
		return EmptyTickList.get();
	}

	@Override
	public ITickList<Fluid> getPendingFluidTicks() {
		return EmptyTickList.get();
	}

	@Override
	public World getWorld() {
		return null;
	}

	@Override
	public WorldInfo getWorldInfo() {
		return null;
	}

	@Override
	public DifficultyInstance getDifficultyForLocation(BlockPos pos) {
		return null;
	}

	@Override
	public AbstractChunkProvider getChunkProvider() {
		return null;
	}

	@Override
	public Random getRandom() {
		return new Random();
	}

	@Override
	public void notifyNeighbors(BlockPos pos, Block blockIn) {
	}

	@Override
	public BlockPos getSpawnPoint() {
		return null;
	}

	@Override
	public void playSound(PlayerEntity player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume,
			float pitch) {
	}

	@Override
	public void addParticle(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed,
			double zSpeed) {
	}

	@Override
	public void playEvent(PlayerEntity player, int type, BlockPos pos, int data) {
	}

	public Cuboid getBounds() {
		return bounds;
	}

	public void setBounds(Cuboid bounds) {
		this.bounds = bounds;
	}

}
