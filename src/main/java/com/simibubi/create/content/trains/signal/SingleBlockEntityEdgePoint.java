package com.simibubi.create.content.trains.signal;

import com.simibubi.create.content.trains.DimensionPalette;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class SingleBlockEntityEdgePoint extends TrackEdgePoint {

	public ResourceKey<Level> blockEntityDimension;
	public BlockPos blockEntityPos;

	public BlockPos getBlockEntityPos() {
		return blockEntityPos;
	}
	
	public ResourceKey<Level> getBlockEntityDimension() {
		return blockEntityDimension;
	}

	@Override
	public void blockEntityAdded(BlockEntity blockEntity, boolean front) {
		this.blockEntityPos = blockEntity.getBlockPos();
		this.blockEntityDimension = blockEntity.getLevel()
			.dimension();
	}

	@Override
	public void blockEntityRemoved(BlockPos blockEntityPos, boolean front) {
		removeFromAllGraphs();
	}

	@Override
	public void invalidate(LevelAccessor level) {
		invalidateAt(level, blockEntityPos);
	}

	@Override
	public boolean canMerge() {
		return false;
	}

	@Override
	public void read(CompoundTag nbt, boolean migration, DimensionPalette dimensions) {
		super.read(nbt, migration, dimensions);
		if (migration)
			return;
		blockEntityPos = NbtUtils.readBlockPos(nbt.getCompound("TilePos"));
		blockEntityDimension = dimensions.decode(nbt.contains("TileDimension") ? nbt.getInt("TileDimension") : -1);
	}

	@Override
	public void write(CompoundTag nbt, DimensionPalette dimensions) {
		super.write(nbt, dimensions);
		nbt.put("TilePos", NbtUtils.writeBlockPos(blockEntityPos));
		nbt.putInt("TileDimension", dimensions.encode(blockEntityDimension));
	}

}
