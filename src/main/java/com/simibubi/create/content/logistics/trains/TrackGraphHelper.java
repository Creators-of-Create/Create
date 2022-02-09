package com.simibubi.create.content.logistics.trains;

import java.util.List;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.TrackNodeLocation.DiscoveredLocation;
import com.simibubi.create.content.logistics.trains.management.GraphLocation;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TrackGraphHelper {

	public static GraphLocation getGraphLocationAt(Level level, BlockPos pos, AxisDirection targetDirection,
		Vec3 targetAxis) {
		BlockState trackBlockState = level.getBlockState(pos);
		if (!(trackBlockState.getBlock()instanceof ITrackBlock track))
			return null;

		Vec3 axis = targetAxis.scale(targetDirection.getStep());
		double length = axis.length();

		List<Pair<BlockPos, DiscoveredLocation>> ends =
			TrackPropagator.getEnds(level, pos, trackBlockState, null, true);

		TrackGraph graph = null;
		TrackNode frontNode = null;
		TrackNode backNode = null;
		double position = 0;

		for (Pair<BlockPos, DiscoveredLocation> pair : ends) {
			DiscoveredLocation current = pair.getSecond();
			BlockPos currentPos = pair.getFirst();
			Vec3 offset = Vec3.atLowerCornerOf(currentPos.subtract(pos));
			boolean forward = offset.distanceToSqr(axis.scale(-1)) < 1 / 4096f;
			boolean backwards = offset.distanceToSqr(axis) < 1 / 4096f;

			if (!forward && !backwards)
				continue;

			for (int i = 0; i < 32; i++) {
				DiscoveredLocation loc = current;
				List<Pair<BlockPos, DiscoveredLocation>> list =
					TrackPropagator.getEnds(level, currentPos, level.getBlockState(currentPos), current, true);
				if (!list.isEmpty()) {
					currentPos = list.get(0)
						.getFirst();
					current = list.get(0)
						.getSecond();
				}

				if (graph == null)
					graph = Create.RAILWAYS.getGraph(level, loc);
				if (graph == null)
					continue;
				TrackNode node = graph.locateNode(loc);
				if (node == null)
					continue;
				if (forward)
					frontNode = node;
				if (backwards) {
					backNode = node;
					position = (i + .5) * length;
				}
				break;
			}
		}

		if (frontNode == null || backNode == null)
			return null;

		GraphLocation graphLocation = new GraphLocation();
		graphLocation.edge = Couple.create(backNode.getLocation(), frontNode.getLocation());
		graphLocation.position = position;
		graphLocation.graph = graph;
		return graphLocation;
	}

}
