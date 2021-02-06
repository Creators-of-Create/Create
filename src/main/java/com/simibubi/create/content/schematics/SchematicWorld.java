package com.simibubi.create.content.schematics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.EmptyTickList;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.ITickList;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeRegistry;
import net.minecraft.world.server.ServerWorld;

public class SchematicWorld extends WrappedWorld implements IServerWorld {

	private Map<BlockPos, BlockState> blocks;
	private Map<BlockPos, TileEntity> tileEntities;
	private List<TileEntity> renderedTileEntities;
	private List<Entity> entities;
	private MutableBoundingBox bounds;
	public BlockPos anchor;
	public boolean renderMode;

	public SchematicWorld(World original) {
		this(BlockPos.ZERO, original);
	}
	
	public SchematicWorld(BlockPos anchor, World original) {
		super(original);
		this.blocks = new HashMap<>();
		this.tileEntities = new HashMap<>();
		this.bounds = new MutableBoundingBox();
		this.anchor = anchor;
		this.entities = new ArrayList<>();
		this.renderedTileEntities = new ArrayList<>();
	}

	public Set<BlockPos> getAllPositions() {
		return blocks.keySet();
	}

	@Override
	public boolean addEntity(Entity entityIn) {
		if (entityIn instanceof ItemFrameEntity)
			((ItemFrameEntity) entityIn).getDisplayedItem()
				.setTag(null);
		if (entityIn instanceof ArmorStandEntity) {
			ArmorStandEntity armorStandEntity = (ArmorStandEntity) entityIn;
			armorStandEntity.getEquipmentAndArmor()
				.forEach(stack -> stack.setTag(null));
		}

		return entities.add(entityIn);
	}

	public Stream<Entity> getEntities() {
		return entities.stream();
	}

	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		if (isOutsideBuildHeight(pos))
			return null;
		if (tileEntities.containsKey(pos))
			return tileEntities.get(pos);
		if (!blocks.containsKey(pos.subtract(anchor)))
			return null;

		BlockState blockState = getBlockState(pos);
		if (blockState.hasTileEntity()) {
			try {
				TileEntity tileEntity = blockState.createTileEntity(this);
				if (tileEntity != null) {
					tileEntity.setLocation(this, pos);
					tileEntities.put(pos, tileEntity);
					renderedTileEntities.add(tileEntity);
				}
				return tileEntity;
			} catch (Exception e) {
				Create.logger.debug("Could not create TE of block " + blockState + ": " + e);
			}
		}
		return null;
	}

	@Override
	public BlockState getBlockState(BlockPos globalPos) {
		BlockPos pos = globalPos.subtract(anchor);

		if (pos.getY() - bounds.minY == -1 && !renderMode)
			return Blocks.GRASS_BLOCK.getDefaultState();
		if (getBounds().isVecInside(pos) && blocks.containsKey(pos)) {
			BlockState blockState = blocks.get(pos);
			if (BlockHelper.hasBlockStateProperty(blockState, BlockStateProperties.LIT))
				blockState = blockState.with(BlockStateProperties.LIT, false);
			return blockState;
		}
		return Blocks.AIR.getDefaultState();
	}

	public Map<BlockPos, BlockState> getBlockMap() {
		return blocks;
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return getBlockState(pos).getFluidState();
	}

	@Override
	public Biome getBiome(BlockPos pos) {
		return BiomeRegistry.THE_VOID;
	}

	@Override
	public int getLightLevel(LightType p_226658_1_, BlockPos p_226658_2_) {
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
	public int getSkylightSubtracted() {
		return 0;
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
		bounds.expandTo(new MutableBoundingBox(pos, pos));
		blocks.put(pos, arg1);
		return true;
	}

	@Override
	public ITickList<Block> getPendingBlockTicks() {
		return EmptyTickList.get();
	}

	@Override
	public ITickList<Fluid> getPendingFluidTicks() {
		return EmptyTickList.get();
	}

	public MutableBoundingBox getBounds() {
		return bounds;
	}

	public Iterable<TileEntity> getRenderedTileEntities() {
		return renderedTileEntities;
	}

	@Override
	public ServerWorld getWorld() {
		if (this.world instanceof ServerWorld) {
			return (ServerWorld) this.world;
		}
		throw new IllegalStateException("Cannot use IServerWorld#getWorld in a client environment");
	}
}
