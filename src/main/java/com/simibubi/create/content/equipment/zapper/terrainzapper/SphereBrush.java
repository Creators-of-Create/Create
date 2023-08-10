package com.simibubi.create.content.equipment.zapper.terrainzapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.utility.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class SphereBrush extends ShapedBrush {

	public static final int MAX_RADIUS = 10;
	private Map<Integer, List<BlockPos>> cachedBrushes;

	public SphereBrush() {
		super(1);

		cachedBrushes = new HashMap<>();
		for (int i = 0; i <= MAX_RADIUS; i++) {
			int radius = i;
			List<BlockPos> positions =
				BlockPos.betweenClosedStream(BlockPos.ZERO.offset(-i - 1, -i - 1, -i - 1), BlockPos.ZERO.offset(i + 1, i + 1, i + 1))
						.map(BlockPos::new).filter(p -> VecHelper.getCenterOf(p)
								.distanceTo(VecHelper.getCenterOf(BlockPos.ZERO)) < radius + .5f)
						.collect(Collectors.toList());
			cachedBrushes.put(i, positions);
		}
	}

	@Override
	public BlockPos getOffset(Vec3 ray, Direction face, PlacementOptions option) {
		if (option == PlacementOptions.Merged)
			return BlockPos.ZERO;

		int offset = option == PlacementOptions.Attached ? 0 : -1;
		int r = (param0 + 1 + offset);

		return BlockPos.ZERO.relative(face, r * (option == PlacementOptions.Attached ? 1 : -1));
	}

	@Override
	int getMax(int paramIndex) {
		return MAX_RADIUS;
	}

	@Override
	Component getParamLabel(int paramIndex) {
		return CreateLang.translateDirect("generic.radius");
	}

	@Override
	List<BlockPos> getIncludedPositions() {
		return cachedBrushes.get(Integer.valueOf(param0));
	}

}
