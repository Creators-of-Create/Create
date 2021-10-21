package com.simibubi.create.foundation.utility.worldWrappers;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.listener.IChunkStatusListener;

public class DummyStatusListener implements IChunkStatusListener {

	@Override
	public void updateSpawnPos(ChunkPos p_219509_1_) {}

	@Override
	public void onStatusChange(ChunkPos p_219508_1_, ChunkStatus p_219508_2_) {}

	@Override
	public void stop() {}

}
