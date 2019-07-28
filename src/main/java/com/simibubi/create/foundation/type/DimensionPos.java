package com.simibubi.create.foundation.type;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DimensionPos {
	public World world;
	public BlockPos pos;

	public DimensionPos(ServerPlayerEntity player, BlockPos pos) {
		this.world = player.world;
		this.pos = pos;
	}
}