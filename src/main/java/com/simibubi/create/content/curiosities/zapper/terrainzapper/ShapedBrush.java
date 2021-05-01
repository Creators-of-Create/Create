package com.simibubi.create.content.curiosities.zapper.terrainzapper;

import java.util.Collection;
import java.util.List;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public abstract class ShapedBrush extends Brush {

	public ShapedBrush(int amtParams) {
		super(amtParams);
	}

	@Override
	public Collection<BlockPos> addToGlobalPositions(IWorld world, BlockPos targetPos, Direction targetFace,
		Collection<BlockPos> affectedPositions, TerrainTools usedTool) {
		List<BlockPos> includedPositions = getIncludedPositions();
		if (includedPositions == null)
			return affectedPositions;
		for (BlockPos blockPos : includedPositions)
			affectedPositions.add(targetPos.add(blockPos));
		return affectedPositions;
	}

	abstract List<BlockPos> getIncludedPositions();

}
