package com.simibubi.create.content.equipment.zapper.terrainzapper;

import java.util.Collection;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;

public abstract class ShapedBrush extends Brush {

	public ShapedBrush(int amtParams) {
		super(amtParams);
	}

	@Override
	public Collection<BlockPos> addToGlobalPositions(LevelAccessor world, BlockPos targetPos, Direction targetFace,
		Collection<BlockPos> affectedPositions, TerrainTools usedTool) {
		List<BlockPos> includedPositions = getIncludedPositions();
		if (includedPositions == null)
			return affectedPositions;
		for (BlockPos blockPos : includedPositions)
			affectedPositions.add(targetPos.offset(blockPos));
		return affectedPositions;
	}

	abstract List<BlockPos> getIncludedPositions();

}
