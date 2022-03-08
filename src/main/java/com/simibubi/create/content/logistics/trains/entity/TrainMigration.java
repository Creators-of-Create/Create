package com.simibubi.create.content.logistics.trains.entity;

import java.util.Map.Entry;

import com.simibubi.create.content.logistics.trains.GraphLocation;
import com.simibubi.create.content.logistics.trains.TrackEdge;
import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.content.logistics.trains.TrackNode;
import com.simibubi.create.content.logistics.trains.TrackNodeLocation;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class TrainMigration {

	Couple<TrackNodeLocation> locations;
	double positionOnOldEdge;
	boolean curve;
	Vec3 fallback;

	public TrainMigration(TravellingPoint point) {
		double t = point.position / point.edge.getLength(point.node1, point.node2);
		fallback = point.edge.getPosition(point.node1, point.node2, t);
		curve = point.edge.isTurn();
		positionOnOldEdge = point.position;
		locations = Couple.create(point.node1.getLocation(), point.node2.getLocation());
	}

	public GraphLocation tryMigratingTo(TrackGraph graph) {
		TrackNode node1 = graph.locateNode(locations.getFirst());
		TrackNode node2 = graph.locateNode(locations.getSecond());
		if (node1 != null && node2 != null) {
			TrackEdge edge = graph.getConnectionsFrom(node1)
				.get(node2);
			if (edge != null) {
				GraphLocation graphLocation = new GraphLocation();
				graphLocation.graph = graph;
				graphLocation.edge = locations;
				graphLocation.position = positionOnOldEdge;
				return graphLocation;
			}
		}

		if (curve)
			return null;

		Vec3 prevDirection = locations.getSecond()
			.getLocation()
			.subtract(locations.getFirst()
				.getLocation())
			.normalize();

		for (TrackNodeLocation loc : graph.getNodes()) {
			Vec3 nodeVec = loc.getLocation();
			if (nodeVec.distanceToSqr(fallback) > 32 * 32)
				continue;

			TrackNode newNode1 = graph.locateNode(loc);
			for (Entry<TrackNode, TrackEdge> entry : graph.getConnectionsFrom(newNode1)
				.entrySet()) {
				TrackEdge edge = entry.getValue();
				if (edge.isTurn())
					continue;
				TrackNode newNode2 = entry.getKey();
				float radius = 1 / 64f;
				Vec3 direction = edge.getDirection(newNode1, newNode2, true);
				if (!Mth.equal(direction.dot(prevDirection), 1))
					continue;
				Vec3 intersectSphere = VecHelper.intersectSphere(nodeVec, direction, fallback, radius);
				if (intersectSphere == null)
					continue;
				if (!Mth.equal(direction.dot(intersectSphere.subtract(nodeVec)
					.normalize()), 1))
					continue;
				double edgeLength = edge.getLength(newNode1, newNode2);
				double position = intersectSphere.distanceTo(nodeVec) - radius;
				if (Double.isNaN(position))
					continue;
				if (position < 0)
					continue;
				if (position > edgeLength)
					continue;

				GraphLocation graphLocation = new GraphLocation();
				graphLocation.graph = graph;
				graphLocation.edge = Couple.create(loc, newNode2.getLocation());
				graphLocation.position = position;
				return graphLocation;
			}
		}

		return null;
	}

}