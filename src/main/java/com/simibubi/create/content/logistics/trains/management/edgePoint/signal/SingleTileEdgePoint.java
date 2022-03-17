package com.simibubi.create.content.logistics.trains.management.edgePoint.signal;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class SingleTileEdgePoint extends TrackEdgePoint {

	public BlockPos tilePos;

	public BlockPos getTilePos() {
		return tilePos;
	}

	@Override
	public void tileAdded(BlockEntity tile, boolean front) {
		this.tilePos = tile.getBlockPos();
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
	public void read(CompoundTag nbt, boolean migration) {
		super.read(nbt, migration);
		if (migration)
			return;
		tilePos = NbtUtils.readBlockPos(nbt.getCompound("TilePos"));
	}

	@Override
	public void write(CompoundTag nbt) {
		super.write(nbt);
		nbt.put("TilePos", NbtUtils.writeBlockPos(tilePos));
	}

}
