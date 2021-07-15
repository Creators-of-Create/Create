package com.simibubi.create.content.curiosities.zapper.terrainzapper;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IWorld;

public class DynamicBrush extends Brush {

	public static final int MAX_RADIUS = 10;

	private boolean surface;

	public DynamicBrush(boolean surface) {
		super(1);
		this.surface = surface;
	}

	ITextComponent getParamLabel(int paramIndex) {
		return Lang.translate("generic.range");
	}

	@Override
	public TerrainTools[] getSupportedTools() {
		return surface ? new TerrainTools[] { TerrainTools.Overlay, TerrainTools.Replace, TerrainTools.Clear }
			: new TerrainTools[] { TerrainTools.Replace, TerrainTools.Clear };
	}

	@Override
	public boolean hasPlacementOptions() {
		return false;
	}

	@Override
	public boolean hasConnectivityOptions() {
		return true;
	}

	@Override
	int getMax(int paramIndex) {
		return MAX_RADIUS;
	}

	@Override
	int getMin(int paramIndex) {
		return 1;
	}

	@Override
	public TerrainTools redirectTool(TerrainTools tool) {
		if (tool == TerrainTools.Overlay)
			return TerrainTools.Place;
		return super.redirectTool(tool);
	}

	@Override
	public Collection<BlockPos> addToGlobalPositions(IWorld world, BlockPos targetPos, Direction targetFace,
		Collection<BlockPos> affectedPositions, TerrainTools usedTool) {

		boolean searchDiagonals = param1 == 0;
		boolean fuzzy = param2 == 0;
		boolean replace = usedTool != TerrainTools.Overlay;
		int searchRange = param0;

		Set<BlockPos> visited = new HashSet<>();
		List<BlockPos> frontier = new LinkedList<>();

		BlockState state = world.getBlockState(targetPos);
		List<BlockPos> offsets = new LinkedList<>();

		for (int x = -1; x <= 1; x++)
			for (int y = -1; y <= 1; y++)
				for (int z = -1; z <= 1; z++)
					if (Math.abs(x) + Math.abs(y) + Math.abs(z) < 2 || searchDiagonals)
						if (targetFace.getAxis()
							.choose(x, y, z) == 0 || !surface)
							offsets.add(new BlockPos(x, y, z));

		BlockPos startPos = replace ? targetPos : targetPos.relative(targetFace);
		frontier.add(startPos);

		while (!frontier.isEmpty()) {
			BlockPos currentPos = frontier.remove(0);
			if (visited.contains(currentPos))
				continue;
			visited.add(currentPos);
			if (!currentPos.closerThan(startPos, searchRange))
				continue;

			// Replace Mode
			if (replace) {
				BlockState stateToReplace = world.getBlockState(currentPos);
				BlockState stateAboveStateToReplace = world.getBlockState(currentPos.relative(targetFace));

				// Criteria
				if (stateToReplace.getDestroySpeed(world, currentPos) == -1)
					continue;
				if (stateToReplace.getBlock() != state.getBlock() && !fuzzy)
					continue;
				if (stateToReplace.getMaterial()
					.isReplaceable())
					continue;
				if (BlockHelper.hasBlockSolidSide(stateAboveStateToReplace, world, currentPos.relative(targetFace),
					targetFace.getOpposite()) && surface)
					continue;
				affectedPositions.add(currentPos);

				// Search adjacent spaces
				for (BlockPos offset : offsets)
					frontier.add(currentPos.offset(offset));
				continue;
			}

			// Place Mode
			BlockState stateToPlaceAt = world.getBlockState(currentPos);
			BlockState stateToPlaceOn = world.getBlockState(currentPos.relative(targetFace.getOpposite()));

			// Criteria
			if (stateToPlaceOn.getMaterial()
				.isReplaceable())
				continue;
			if (stateToPlaceOn.getBlock() != state.getBlock() && !fuzzy)
				continue;
			if (!stateToPlaceAt.getMaterial()
				.isReplaceable())
				continue;
			affectedPositions.add(currentPos);

			// Search adjacent spaces
			for (BlockPos offset : offsets)
				frontier.add(currentPos.offset(offset));
			continue;
		}

		return affectedPositions;
	}
}
