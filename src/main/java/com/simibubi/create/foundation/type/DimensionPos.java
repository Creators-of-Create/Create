package com.simibubi.create.foundation.type;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorld;

public class DimensionPos {
	public ServerWorld world;
	public BlockPos pos;

	public DimensionPos(ServerPlayerEntity player, BlockPos pos) {
		this.world = player.getServerWorld();
		this.pos = pos;
	}
}