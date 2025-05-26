package com.simibubi.create.content.trains.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

public interface PlatformMapData {

	boolean toggleStation(LevelAccessor level, BlockPos pos, PlatformBlockEntity stationBlockEntity);

	void addStationMarker(PlatformMarker marker);

}
