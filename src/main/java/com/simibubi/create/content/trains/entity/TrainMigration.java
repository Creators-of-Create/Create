package com.simibubi.create.content.trains.entity;

import java.util.Map.Entry;

import com.simibubi.create.content.trains.DimensionPalette;
import com.simibubi.create.content.trains.graph.GraphLocation;
import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.graph.TrackNodeLocation;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class TrainMigration {

	Couple<TrackNodeLocation> locations;
	double positionOnOldEdge;
	boolean curve;
	Vec3 fallback;

	public TrainMigration() {}

	public TrainMigration(TravellingPoint point) {
		double t = point.position / point.edge.getLength();
		fallback = point.edge.getPosition(null, t);
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
				Vec3 direction = edge.getDirection(true);
				if (!Mth.equal(direction.dot(prevDirection), 1))
					continue;
				Vec3 intersectSphere = VecHelper.intersectSphere(nodeVec, direction, fallback, radius);
				if (intersectSphere == null)
					continue;
				if (!Mth.equal(direction.dot(intersectSphere.subtract(nodeVec)
					.normalize()), 1))
					continue;
				double edgeLength = edge.getLength();
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

	public CompoundTag write(DimensionPalette dimensions) {
		CompoundTag tag = new CompoundTag();
		tag.putBoolean("Curve", curve);
		tag.put("Fallback", VecHelper.writeNBT(fallback));
		tag.putDouble("Position", positionOnOldEdge);
		tag.put("Nodes", locations.serializeEach(l -> l.write(dimensions)));
		return tag;
	}

	public static TrainMigration read(CompoundTag tag, DimensionPalette dimensions) {
		TrainMigration trainMigration = new TrainMigration();
		trainMigration.curve = tag.getBoolean("Curve");
		trainMigration.fallback = VecHelper.readNBT(tag.getList("Fallback", Tag.TAG_DOUBLE));
		trainMigration.positionOnOldEdge = tag.getDouble("Position");
		trainMigration.locations =
			Couple.deserializeEach(tag.getList("Nodes", Tag.TAG_COMPOUND), c -> TrackNodeLocation.read(c, dimensions));
		return trainMigration;
	}

}