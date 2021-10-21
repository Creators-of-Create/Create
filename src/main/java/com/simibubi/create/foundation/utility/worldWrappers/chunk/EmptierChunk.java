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

	public BlockState getBlockState(BlockPos p_180495_1_) {
		return Blocks.VOID_AIR.defaultBlockState();
	}

	@Nullable
	public BlockState setBlockState(BlockPos p_177436_1_, BlockState p_177436_2_, boolean p_177436_3_) {
		return null;
	}

	public FluidState getFluidState(BlockPos p_204610_1_) {
		return Fluids.EMPTY.defaultFluidState();
	}

	@Nullable
	public WorldLightManager getLightEngine() {
		return null;
	}

	public int getLightEmission(BlockPos p_217298_1_) {
		return 0;
	}

	public void addEntity(Entity p_76612_1_) { }

	public void removeEntity(Entity p_76622_1_) { }

	public void removeEntity(Entity p_76608_1_, int p_76608_2_) { }

	@Nullable
	public TileEntity getBlockEntity(BlockPos p_177424_1_, Chunk.CreateEntityType p_177424_2_) {
		return null;
	}

	public void addBlockEntity(TileEntity p_150813_1_) { }

	public void setBlockEntity(BlockPos p_177426_1_, TileEntity p_177426_2_) { }

	public void removeBlockEntity(BlockPos p_177425_1_) { }

	public void markUnsaved() { }

	public void getEntities(@Nullable Entity p_177414_1_, AxisAlignedBB p_177414_2_, List<Entity> p_177414_3_, Predicate<? super Entity> p_177414_4_) { }

	public <T extends Entity> void getEntitiesOfClass(Class<? extends T> p_177430_1_, AxisAlignedBB p_177430_2_, List<T> p_177430_3_, Predicate<? super T> p_177430_4_) { }

	public boolean isEmpty() {
		return true;
	}

	public boolean isYSpaceEmpty(int p_76606_1_, int p_76606_2_) {
		return true;
	}

	public ChunkHolder.LocationType getFullStatus() {
		return ChunkHolder.LocationType.BORDER;
	}
}
