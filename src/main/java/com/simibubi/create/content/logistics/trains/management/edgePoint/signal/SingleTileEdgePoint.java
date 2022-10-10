package com.simibubi.create.content.logistics.trains.management.edgePoint.signal;

import com.simibubi.create.content.logistics.trains.DimensionPalette;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class SingleTileEdgePoint extends TrackEdgePoint {

	public ResourceKey<Level> tileDimension;
	public BlockPos tilePos;

	public BlockPos getTilePos() {
		return tilePos;
	}
	
	public ResourceKey<Level> getTileDimension() {
		return tileDimension;
	}

	@Override
	public void tileAdded(BlockEntity tile, boolean front) {
		this.tilePos = tile.getBlockPos();
		this.tileDimension = tile.getLevel()
			.dimension();
	}

	@Override
	public void tileRemoved(BlockPos tilePos, boolean front) {
		removeFromAllGraphs();
	}

	@Override
	public void invalidate(LevelAccessor level) {
		invalidateAt(level, tilePos);
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
		tilePos = NbtUtils.readBlockPos(nbt.getCompound("TilePos"));
		tileDimension = dimensions.decode(nbt.contains("TileDimension") ? nbt.getInt("TileDimension") : -1);
	}

	@Override
	public void write(CompoundTag nbt, DimensionPalette dimensions) {
		super.write(nbt, dimensions);
		nbt.put("TilePos", NbtUtils.writeBlockPos(tilePos));
		nbt.putInt("TileDimension", dimensions.encode(tileDimension));
	}

}
