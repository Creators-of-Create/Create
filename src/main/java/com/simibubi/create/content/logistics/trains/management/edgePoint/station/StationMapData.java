package com.simibubi.create.content.logistics.trains.management.edgePoint.station;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

public interface StationMapData {

	boolean toggleStation(LevelAccessor level, BlockPos pos, StationTileEntity stationTileEntity);

	void addStationMarker(StationMarker marker);

}
