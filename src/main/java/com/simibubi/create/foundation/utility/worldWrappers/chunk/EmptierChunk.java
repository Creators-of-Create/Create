package com.simibubi.create.foundation.utility.worldWrappers.chunk;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.server.ChunkHolder;

public class EmptierChunk extends Chunk {

	public EmptierChunk() {
		super(null, null, null);
	}

	public BlockState getBlockState(BlockPos pPos) {
		return Blocks.VOID_AIR.defaultBlockState();
	}

	@Nullable
	public BlockState setBlockState(BlockPos pPos, BlockState pState, boolean pIsMoving) {
		return null;
	}

	public FluidState getFluidState(BlockPos pPos) {
		return Fluids.EMPTY.defaultFluidState();
	}

	@Nullable
	public WorldLightManager getLightEngine() {
		return null;
	}

	public int getLightEmission(BlockPos pPos) {
		return 0;
	}

	public void addEntity(Entity pEntityIn) { }

	public void removeEntity(Entity pEntityIn) { }

	public void removeEntity(Entity pEntityIn, int pIndex) { }

	@Nullable
	public TileEntity getBlockEntity(BlockPos pPos, Chunk.CreateEntityType pCreationMode) {
		return null;
	}

	public void addBlockEntity(TileEntity pTileEntityIn) { }

	public void setBlockEntity(BlockPos pPos, TileEntity pTileEntityIn) { }

	public void removeBlockEntity(BlockPos pPos) { }

	public void markUnsaved() { }

	public void getEntities(@Nullable Entity pEntityIn, AxisAlignedBB pAabb, List<Entity> pListToFill, Predicate<? super Entity> pFilter) { }

	public <T extends Entity> void getEntitiesOfClass(Class<? extends T> pEntityClass, AxisAlignedBB pAabb, List<T> pListToFill, Predicate<? super T> pFilter) { }

	public boolean isEmpty() {
		return true;
	}

	public boolean isYSpaceEmpty(int pStartY, int pEndY) {
		return true;
	}

	public ChunkHolder.LocationType getFullStatus() {
		return ChunkHolder.LocationType.BORDER;
	}
}
