package com.simibubi.create.content.equipment.zapper.terrainzapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FlattenTool {

	// gaussian with sig=1
	static float[][] kernel = {

			{ 0.003765f, 0.015019f, 0.023792f, 0.015019f, 0.003765f },
			{ 0.015019f, 0.059912f, 0.094907f, 0.059912f, 0.015019f },
			{ 0.023792f, 0.094907f, 0.150342f, 0.094907f, 0.023792f },
			{ 0.015019f, 0.059912f, 0.094907f, 0.059912f, 0.015019f },
			{ 0.003765f, 0.015019f, 0.023792f, 0.015019f, 0.003765f },

	};

	private static int[][] applyKernel(int[][] values) {
		int[][] result = new int[values.length][values[0].length];
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < values[i].length; j++) {
				int value = values[i][j];
				float newValue = 0;
				for (int iOffset = -2; iOffset <= 2; iOffset++) {
					for (int jOffset = -2; jOffset <= 2; jOffset++) {
						int iTarget = i + iOffset;
						int jTarget = j + jOffset;
						int ref = 0;
						if (iTarget < 0 || iTarget >= values.length || jTarget < 0 || jTarget >= values[0].length)
							ref = value;
						else
							ref = values[iTarget][jTarget];
						if (ref == Integer.MIN_VALUE)
							ref = value;
						newValue += kernel[iOffset + 2][jOffset + 2] * ref;
					}
				}
				result[i][j] = Mth.floor(newValue + .5f);
			}
		}
		return result;
	}

	public static void apply(Level world, List<BlockPos> targetPositions, Direction facing) {
		List<BlockPos> surfaces = new ArrayList<>();
		Map<Pair<Integer, Integer>, Integer> heightMap = new HashMap<>();
		int offset = facing.getAxisDirection().getStep();

		int minEntry = Integer.MAX_VALUE;
		int minCoord1 = Integer.MAX_VALUE;
		int minCoord2 = Integer.MAX_VALUE;
		int maxEntry = Integer.MIN_VALUE;
		int maxCoord1 = Integer.MIN_VALUE;
		int maxCoord2 = Integer.MIN_VALUE;

		for (BlockPos p : targetPositions) {
			Pair<Integer, Integer> coords = getCoords(p, facing);
			BlockState belowSurface = world.getBlockState(p);

			minCoord1 = Math.min(minCoord1, coords.getKey());
			minCoord2 = Math.min(minCoord2, coords.getValue());
			maxCoord1 = Math.max(maxCoord1, coords.getKey());
			maxCoord2 = Math.max(maxCoord2, coords.getValue());

			if (TerrainTools.isReplaceable(belowSurface)) {
				if (!heightMap.containsKey(coords))
					heightMap.put(coords, Integer.MIN_VALUE);
				continue;
			}

			p = p.relative(facing);
			BlockState surface = world.getBlockState(p);

			if (!TerrainTools.isReplaceable(surface)) {
				if (!heightMap.containsKey(coords) || heightMap.get(coords).equals(Integer.MIN_VALUE))
					heightMap.put(coords, Integer.MAX_VALUE);
				continue;
			}

			surfaces.add(p);
			int coordinate = facing.getAxis().choose(p.getX(), p.getY(), p.getZ());
			if (!heightMap.containsKey(coords) || heightMap.get(coords).equals(Integer.MAX_VALUE)
					|| heightMap.get(coords).equals(Integer.MIN_VALUE)
					|| heightMap.get(coords) * offset < coordinate * offset) {
				heightMap.put(coords, coordinate);
				maxEntry = Math.max(maxEntry, coordinate);
				minEntry = Math.min(minEntry, coordinate);
			}
		}

		if (surfaces.isEmpty())
			return;

		// fill heightmap
		int[][] heightMapArray = new int[maxCoord1 - minCoord1 + 1][maxCoord2 - minCoord2 + 1];
		for (int i = 0; i < heightMapArray.length; i++) {
			for (int j = 0; j < heightMapArray[i].length; j++) {
				Pair<Integer, Integer> pair = Pair.of(minCoord1 + i, minCoord2 + j);
				if (!heightMap.containsKey(pair)) {
					heightMapArray[i][j] = Integer.MIN_VALUE;
					continue;
				}
				Integer height = heightMap.get(pair);
				if (height.equals(Integer.MAX_VALUE)) {
					heightMapArray[i][j] = offset == 1 ? maxEntry + 2 : minEntry - 2;
					continue;
				}
				if (height.equals(Integer.MIN_VALUE)) {
					heightMapArray[i][j] = offset == 1 ? minEntry - 2 : maxEntry + 2;
					continue;
				}

				heightMapArray[i][j] = height;
			}
		}

		heightMapArray = applyKernel(heightMapArray);

		for (BlockPos p : surfaces) {
			Pair<Integer, Integer> coords = getCoords(p, facing);
			int surfaceCoord = facing.getAxis().choose(p.getX(), p.getY(), p.getZ()) * offset;
			int targetCoord = heightMapArray[coords.getKey() - minCoord1][coords.getValue() - minCoord2] * offset;

			// Keep surface
			if (surfaceCoord == targetCoord)
				continue;

			// Lower surface
			BlockState blockState = world.getBlockState(p);
			int timeOut = 1000;
			while (surfaceCoord > targetCoord) {
				BlockPos below = p.relative(facing.getOpposite());
				world.setBlockAndUpdate(below, blockState);
				world.setBlockAndUpdate(p, blockState.getFluidState().createLegacyBlock());
				p = p.relative(facing.getOpposite());
				surfaceCoord--;
				if (timeOut-- <= 0)
					break;
			}

			// Raise surface
			while (surfaceCoord < targetCoord) {
				BlockPos above = p.relative(facing);
				if (!(blockState.getBlock() instanceof LiquidBlock))
					world.setBlockAndUpdate(above, blockState);
				world.setBlockAndUpdate(p, world.getBlockState(p.relative(facing.getOpposite())));
				p = p.relative(facing);
				surfaceCoord++;
				if (timeOut-- <= 0)
					break;
			}

		}
	}

	private static Pair<Integer, Integer> getCoords(BlockPos pos, Direction facing) {
		switch (facing.getAxis()) {
		case X:
			return Pair.of(pos.getZ(), pos.getY());
		case Y:
			return Pair.of(pos.getX(), pos.getZ());
		case Z:
			return Pair.of(pos.getX(), pos.getY());
		}
		return null;
	}

}
