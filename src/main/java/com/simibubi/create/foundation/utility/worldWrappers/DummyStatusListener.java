package com.simibubi.create.foundation.utility.worldWrappers;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.server.level.progress.ChunkProgressListener;

public class DummyStatusListener implements ChunkProgressListener {

	@Override
	public void updateSpawnPos(ChunkPos p_219509_1_) {}

	@Override
	public void onStatusChange(ChunkPos p_219508_1_, ChunkStatus p_219508_2_) {}

	@Override
	public void stop() {}

}
