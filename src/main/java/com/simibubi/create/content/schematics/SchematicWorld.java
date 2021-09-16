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
import com.simibubi.create.foundation.utility.worldWrappers.WrappedChunkProvider;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;

import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.EmptyTickList;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.data.worldgen.biome.Biomes;
import net.minecraft.server.level.ServerLevel;

public class SchematicWorld extends WrappedWorld implements ServerLevelAccessor {

	protected Map<BlockPos, BlockState> blocks;
	protected Map<BlockPos, BlockEntity> tileEntities;
	protected List<BlockEntity> renderedTileEntities;
	protected List<Entity> entities;
	protected BoundingBox bounds;

	public BlockPos anchor;
	public boolean renderMode;

	public SchematicWorld(Level original) {
		this(BlockPos.ZERO, original);
	}

	public SchematicWorld(BlockPos anchor, Level original) {
		super(original, new WrappedChunkProvider());
		this.blocks = new HashMap<>();
		this.tileEntities = new HashMap<>();
		this.bounds = new BoundingBox();
		this.anchor = anchor;
		this.entities = new ArrayList<>();
		this.renderedTileEntities = new ArrayList<>();
	}

	public Set<BlockPos> getAllPositions() {
		return blocks.keySet();
	}

	@Override
	public boolean addFreshEntity(Entity entityIn) {
		if (entityIn instanceof ItemFrame)
			((ItemFrame) entityIn).getItem()
				.setTag(null);
		if (entityIn instanceof ArmorStand) {
			ArmorStand armorStandEntity = (ArmorStand) entityIn;
			armorStandEntity.getAllSlots()
				.forEach(stack -> stack.setTag(null));
		}

		return entities.add(entityIn);
	}

	public Stream<Entity> getEntities() {
		return entities.stream();
	}

	@Override
	public BlockEntity getBlockEntity(BlockPos pos) {
		if (isOutsideBuildHeight(pos))
			return null;
		if (tileEntities.containsKey(pos))
			return tileEntities.get(pos);
		if (!blocks.containsKey(pos.subtract(anchor)))
			return null;

		BlockState blockState = getBlockState(pos);
		if (blockState.hasTileEntity()) {
			try {
				BlockEntity tileEntity = blockState.createTileEntity(this);
				if (tileEntity != null) {
					onTEadded(tileEntity, pos);
					tileEntities.put(pos, tileEntity);
					renderedTileEntities.add(tileEntity);
				}
				return tileEntity;
			} catch (Exception e) {
				Create.LOGGER.debug("Could not create TE of block " + blockState + ": " + e);
			}
		}
		return null;
	}

	protected void onTEadded(BlockEntity tileEntity, BlockPos pos) {
		tileEntity.setLevelAndPosition(this, pos);
	}

	@Override
	public BlockState getBlockState(BlockPos globalPos) {
		BlockPos pos = globalPos.subtract(anchor);

		if (pos.getY() - bounds.y0 == -1 && !renderMode)
			return Blocks.GRASS_BLOCK.defaultBlockState();
		if (getBounds().isInside(pos) && blocks.containsKey(pos))
			return processBlockStateForPrinting(blocks.get(pos));
		return Blocks.AIR.defaultBlockState();
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
		return Biomes.THE_VOID;
	}

	@Override
	public int getBrightness(LightLayer p_226658_1_, BlockPos p_226658_2_) {
		return 10;
	}

	@Override
	public List<Entity> getEntities(Entity arg0, AABB arg1, Predicate<? super Entity> arg2) {
		return Collections.emptyList();
	}

	@Override
	public <T extends Entity> List<T> getEntitiesOfClass(Class<? extends T> arg0, AABB arg1,
		Predicate<? super T> arg2) {
		return Collections.emptyList();
	}

	@Override
	public List<? extends Player> players() {
		return Collections.emptyList();
	}

	@Override
	public int getSkyDarken() {
		return 0;
	}

	@Override
	public boolean isStateAtPosition(BlockPos pos, Predicate<BlockState> predicate) {
		return predicate.test(getBlockState(pos));
	}

	@Override
	public boolean destroyBlock(BlockPos arg0, boolean arg1) {
		return setBlock(arg0, Blocks.AIR.defaultBlockState(), 3);
	}

	@Override
	public boolean removeBlock(BlockPos arg0, boolean arg1) {
		return setBlock(arg0, Blocks.AIR.defaultBlockState(), 3);
	}

	@Override
	public boolean setBlock(BlockPos pos, BlockState arg1, int arg2) {
		pos = pos.immutable()
			.subtract(anchor);
		bounds.expand(new BoundingBox(pos, pos));
		blocks.put(pos, arg1);
		if (tileEntities.containsKey(pos)) {
			BlockEntity tileEntity = tileEntities.get(pos);
			if (!tileEntity.getType()
				.isValid(arg1.getBlock())) {
				tileEntities.remove(pos);
				renderedTileEntities.remove(tileEntity);
			}
		}

		BlockEntity tileEntity = getBlockEntity(pos);
		if (tileEntity != null)
			tileEntities.put(pos, tileEntity);

		return true;
	}

	@Override
	public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) { }

	@Override
	public TickList<Block> getBlockTicks() {
		return EmptyTickList.empty();
	}

	@Override
	public TickList<Fluid> getLiquidTicks() {
		return EmptyTickList.empty();
	}

	public BoundingBox getBounds() {
		return bounds;
	}

	public Iterable<BlockEntity> getRenderedTileEntities() {
		return renderedTileEntities;
	}

	protected BlockState processBlockStateForPrinting(BlockState state) {
		if (state.getBlock() instanceof AbstractFurnaceBlock && state.hasProperty(BlockStateProperties.LIT))
			state = state.setValue(BlockStateProperties.LIT, false);
		return state;
	}

	@Override
	public ServerLevel getLevel() {
		if (this.world instanceof ServerLevel) {
			return (ServerLevel) this.world;
		}
		throw new IllegalStateException("Cannot use IServerWorld#getWorld in a client environment");
	}

}
