package com.simibubi.create.content.trains.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.simibubi.create.content.trains.track.BezierConnection;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TrackGraphBounds {

	public AABB box;
	public List<BezierConnection> beziers;

	public TrackGraphBounds(TrackGraph graph, ResourceKey<Level> dimension) {
		beziers = new ArrayList<>();
		box = null;

		for (TrackNode node : graph.nodes.values()) {
			if (node.location.dimension != dimension)
				continue;
			include(node);
			Map<TrackNode, TrackEdge> connections = graph.getConnectionsFrom(node);
			for (TrackEdge edge : connections.values())
				if (edge.turn != null && edge.turn.isPrimary())
					beziers.add(edge.turn);
		}

		if (box != null)
			box = box.inflate(2);
	}

	private void include(TrackNode node) {
		Vec3 v = node.location.getLocation();
		AABB aabb = new AABB(v, v);
		box = box == null ? aabb : box.minmax(aabb);
	}

}
