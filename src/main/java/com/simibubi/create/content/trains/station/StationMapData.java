package com.simibubi.create.content.trains.station;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

public interface StationMapData {

	boolean toggleStation(LevelAccessor level, BlockPos pos, StationBlockEntity stationBlockEntity);

	void addStationMarker(StationMarker marker);

}
