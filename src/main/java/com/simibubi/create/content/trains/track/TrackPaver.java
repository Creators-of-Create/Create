package com.simibubi.create.content.trains.track;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.girder.GirderBlock;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;

import net.createmod.catnip.utility.Iterate;
import net.createmod.catnip.utility.Pair;
import net.createmod.catnip.utility.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;

public class TrackPaver {

	public static int paveStraight(Level level, BlockPos startPos, Vec3 direction, int extent, Block block,
		boolean simulate, Set<BlockPos> visited) {
		int itemsNeeded = 0;

		BlockState defaultBlockState = block.defaultBlockState();
		boolean slabLike = defaultBlockState.hasProperty(SlabBlock.TYPE);
		boolean wallLike = isWallLike(defaultBlockState);

		if (slabLike)
			defaultBlockState = defaultBlockState.setValue(SlabBlock.TYPE, SlabType.DOUBLE);

		if (defaultBlockState.getBlock() instanceof GirderBlock)
			for (Direction d : Iterate.horizontalDirections)
				if (Vec3.atLowerCornerOf(d.getNormal())
					.equals(direction))
					defaultBlockState = defaultBlockState.setValue(GirderBlock.TOP, false)
						.setValue(GirderBlock.BOTTOM, false)
						.setValue(GirderBlock.AXIS, d.getAxis())
						.setValue(d.getAxis() == Axis.X ? GirderBlock.X : GirderBlock.Z, true);

		Set<BlockPos> toPlaceOn = new HashSet<>();
		Vec3 start = VecHelper.getCenterOf(startPos);
		Vec3 mainNormal = direction.cross(new Vec3(0, 1, 0));
		Vec3 normalizedNormal = mainNormal.normalize();
		Vec3 normalizedDirection = direction.normalize();

		float diagFiller = 0.45f;
		for (int i = 0; i < extent; i++) {
			Vec3 offset = direction.scale(i);
			Vec3 mainPos = start.add(offset.x, offset.y, offset.z);
			toPlaceOn.add(BlockPos.containing(mainPos.add(mainNormal)));
			toPlaceOn.add(BlockPos.containing(mainPos.subtract(mainNormal)));
			if (wallLike)
				continue;
			toPlaceOn.add(BlockPos.containing(mainPos));
			if (i < extent - 1)
				for (int x : Iterate.positiveAndNegative)
					toPlaceOn.add(BlockPos.containing(mainPos.add(normalizedNormal.scale(x * diagFiller))
						.add(normalizedDirection.scale(diagFiller))));
			if (i > 0)
				for (int x : Iterate.positiveAndNegative)
					toPlaceOn.add(BlockPos.containing(mainPos.add(normalizedNormal.scale(x * diagFiller))
						.add(normalizedDirection.scale(-diagFiller))));
		}

		final BlockState state = defaultBlockState;
		for (BlockPos p : toPlaceOn) {
			if (!visited.add(p))
				continue;
			if (placeBlockIfFree(level, p, state, simulate))
				itemsNeeded += slabLike ? 2 : 1;
		}
		visited.addAll(toPlaceOn);

		return itemsNeeded;
	}

	public static int paveCurve(Level level, BezierConnection bc, Block block, boolean simulate,
		Set<BlockPos> visited) {
		int itemsNeeded = 0;

		BlockState defaultBlockState = block.defaultBlockState();
		boolean slabLike = defaultBlockState.hasProperty(SlabBlock.TYPE);
		if (slabLike)
			defaultBlockState = defaultBlockState.setValue(SlabBlock.TYPE, SlabType.DOUBLE);
		if (isWallLike(defaultBlockState)) {
			if (AllBlocks.METAL_GIRDER.has(defaultBlockState))
				return ((bc.getSegmentCount() + 1) / 2) * 2;
			return 0;
		}

		Map<Pair<Integer, Integer>, Double> yLevels = new HashMap<>();
		BlockPos tePosition = bc.tePositions.getFirst();
		Vec3 end1 = bc.starts.getFirst()
			.subtract(Vec3.atLowerCornerOf(tePosition))
			.add(0, 3 / 16f, 0);
		Vec3 end2 = bc.starts.getSecond()
			.subtract(Vec3.atLowerCornerOf(tePosition))
			.add(0, 3 / 16f, 0);
		Vec3 axis1 = bc.axes.getFirst();
		Vec3 axis2 = bc.axes.getSecond();

		double handleLength = bc.getHandleLength();

		Vec3 finish1 = axis1.scale(handleLength)
			.add(end1);
		Vec3 finish2 = axis2.scale(handleLength)
			.add(end2);

		Vec3 faceNormal1 = bc.normals.getFirst();
		Vec3 faceNormal2 = bc.normals.getSecond();

		int segCount = bc.getSegmentCount();
		float[] lut = bc.getStepLUT();

		for (int i = 0; i < segCount; i++) {
			float t = i == segCount ? 1 : i * lut[i] / segCount;
			t += 0.5f / segCount;

			Vec3 result = VecHelper.bezier(end1, end2, finish1, finish2, t);
			Vec3 derivative = VecHelper.bezierDerivative(end1, end2, finish1, finish2, t)
				.normalize();
			Vec3 faceNormal =
				faceNormal1.equals(faceNormal2) ? faceNormal1 : VecHelper.slerp(t, faceNormal1, faceNormal2);
			Vec3 normal = faceNormal.cross(derivative)
				.normalize();
			Vec3 below = result.add(faceNormal.scale(-1.125f));
			Vec3 rail1 = below.add(normal.scale(.97f));
			Vec3 rail2 = below.subtract(normal.scale(.97f));
			Vec3 railMiddle = rail1.add(rail2)
				.scale(.5);

			for (Vec3 vec : new Vec3[] { rail1, rail2, railMiddle }) {
				BlockPos pos = BlockPos.containing(vec);
				Pair<Integer, Integer> key = Pair.of(pos.getX(), pos.getZ());
				if (!yLevels.containsKey(key) || yLevels.get(key) > vec.y)
					yLevels.put(key, vec.y);
			}
		}

		for (Entry<Pair<Integer, Integer>, Double> entry : yLevels.entrySet()) {
			double yValue = entry.getValue();
			int floor = Mth.floor(yValue);
			boolean placeSlab = slabLike && yValue - floor >= .5;
			BlockPos targetPos = new BlockPos(entry.getKey()
				.getFirst(), floor,
				entry.getKey()
					.getSecond());
			targetPos = targetPos.offset(tePosition)
				.above(placeSlab ? 1 : 0);
			BlockState stateToPlace =
				placeSlab ? defaultBlockState.setValue(SlabBlock.TYPE, SlabType.BOTTOM) : defaultBlockState;
			if (!visited.add(targetPos))
				continue;
			if (placeBlockIfFree(level, targetPos, stateToPlace, simulate))
				itemsNeeded += !placeSlab ? 2 : 1;
			if (placeSlab) {
				if (!visited.add(targetPos.below()))
					continue;
				BlockState topSlab = stateToPlace.setValue(SlabBlock.TYPE, SlabType.TOP);
				if (placeBlockIfFree(level, targetPos.below(), topSlab, simulate))
					itemsNeeded++;
			}
		}

		return itemsNeeded;
	}

	private static boolean isWallLike(BlockState defaultBlockState) {
		return defaultBlockState.getBlock() instanceof WallBlock || AllBlocks.METAL_GIRDER.has(defaultBlockState);
	}

	private static boolean placeBlockIfFree(Level level, BlockPos pos, BlockState state, boolean simulate) {
		BlockState stateAtPos = level.getBlockState(pos);
		if (stateAtPos.getBlock() != state.getBlock() && stateAtPos.canBeReplaced()) {
			if (!simulate)
				level.setBlock(pos, ProperWaterloggedBlock.withWater(level, state, pos), 3);
			return true;
		}
		return false;
	}

}
