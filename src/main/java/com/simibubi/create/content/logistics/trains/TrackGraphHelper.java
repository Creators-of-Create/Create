package com.simibubi.create.content.logistics.trains;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.TrackNodeLocation.DiscoveredLocation;
import com.simibubi.create.content.logistics.trains.track.BezierTrackPointLocation;
import com.simibubi.create.content.logistics.trains.track.TrackTileEntity;
import com.simibubi.create.foundation.utility.Couple;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TrackGraphHelper {

	@Nullable
	public static GraphLocation getGraphLocationAt(Level level, BlockPos pos, AxisDirection targetDirection,
		Vec3 targetAxis) {
		BlockState trackBlockState = level.getBlockState(pos);
		if (!(trackBlockState.getBlock()instanceof ITrackBlock track))
			return null;

		Vec3 axis = targetAxis.scale(targetDirection.getStep());
		double length = axis.length();
		TrackGraph graph = null;

		// Case 1: Centre of block lies on a node

		TrackNodeLocation location = new TrackNodeLocation(Vec3.atBottomCenterOf(pos)
			.add(0, track.getElevationAtCenter(level, pos, trackBlockState), 0)).in(level);
		graph = Create.RAILWAYS.sided(level)
			.getGraph(level, location);
		if (graph != null) {
			TrackNode node = graph.locateNode(location);
			if (node != null) {
				Map<TrackNode, TrackEdge> connectionsFrom = graph.getConnectionsFrom(node);
				for (Entry<TrackNode, TrackEdge> entry : connectionsFrom.entrySet()) {
					TrackNode backNode = entry.getKey();
					Vec3 direction = entry.getValue()
						.getDirection(true);
					if (direction.scale(length)
						.distanceToSqr(axis.scale(-1)) > 1 / 4096f)
						continue;

					GraphLocation graphLocation = new GraphLocation();
					graphLocation.edge = Couple.create(node.getLocation(), backNode.getLocation());
					graphLocation.position = 0;
					graphLocation.graph = graph;
					return graphLocation;
				}
			}
		}

		// Case 2: Center of block is between two nodes

		Collection<DiscoveredLocation> ends = track.getConnected(level, pos, trackBlockState, true, null);
		Vec3 start = Vec3.atBottomCenterOf(pos)
			.add(0, track.getElevationAtCenter(level, pos, trackBlockState), 0);

		TrackNode frontNode = null;
		TrackNode backNode = null;
		double position = 0;

		for (DiscoveredLocation current : ends) {
			Vec3 offset = current.getLocation()
				.subtract(start)
				.normalize()
				.scale(length);

			boolean forward = offset.distanceToSqr(axis.scale(-1)) < 1 / 4096f;
			boolean backwards = offset.distanceToSqr(axis) < 1 / 4096f;

			if (!forward && !backwards)
				continue;

			DiscoveredLocation previous = null;
			double distance = 0;
			for (int i = 0; i < 100 && distance < 32; i++) {
				DiscoveredLocation loc = current;
				if (graph == null)
					graph = Create.RAILWAYS.sided(level)
						.getGraph(level, loc);

				if (graph == null || graph.locateNode(loc) == null) {
					Collection<DiscoveredLocation> list = ITrackBlock.walkConnectedTracks(level, loc, true);
					for (DiscoveredLocation discoveredLocation : list) {
						if (discoveredLocation == previous)
							continue;
						Vec3 diff = discoveredLocation.getLocation()
							.subtract(loc.getLocation());
						if ((forward ? axis.scale(-1) : axis).distanceToSqr(diff.normalize()
							.scale(length)) > 1 / 4096f)
							continue;

						previous = current;
						current = discoveredLocation;
						distance += diff.length();
						break;
					}
					continue;
				}

				TrackNode node = graph.locateNode(loc);
				if (forward)
					frontNode = node;
				if (backwards) {
					backNode = node;
					position = distance + axis.length() / 2;
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

	@Nullable
	public static GraphLocation getBezierGraphLocationAt(Level level, BlockPos pos, AxisDirection targetDirection,
		BezierTrackPointLocation targetBezier) {
		BlockState state = level.getBlockState(pos);

		if (!(state.getBlock()instanceof ITrackBlock track))
			return null;
		if (!(level.getBlockEntity(pos)instanceof TrackTileEntity trackTE))
			return null;
		BezierConnection bc = trackTE.getConnections()
			.get(targetBezier.curveTarget());
		if (bc == null || !bc.isPrimary())
			return null;

		TrackNodeLocation targetLoc = new TrackNodeLocation(bc.starts.getSecond()).in(level);
		for (DiscoveredLocation location : track.getConnected(level, pos, state, true, null)) {
			TrackGraph graph = Create.RAILWAYS.sided(level)
				.getGraph(level, location);
			if (graph == null)
				continue;
			TrackNode targetNode = graph.locateNode(targetLoc);
			if (targetNode == null)
				continue;
			TrackNode node = graph.locateNode(location);
			TrackEdge edge = graph.getConnectionsFrom(node)
				.get(targetNode);
			if (edge == null)
				continue;

			GraphLocation graphLocation = new GraphLocation();
			graphLocation.graph = graph;
			graphLocation.edge = Couple.create(location, targetLoc);
			graphLocation.position = (targetBezier.segment() + 1) / 2f;
			if (targetDirection == AxisDirection.POSITIVE) {
				graphLocation.edge = graphLocation.edge.swap();
				graphLocation.position = edge.getLength() - graphLocation.position;
			}

			return graphLocation;
		}

		return null;
	}

}
