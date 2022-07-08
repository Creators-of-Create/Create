package com.simibubi.create.content.logistics.trains;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.lwjgl.glfw.GLFW;

import com.simibubi.create.AllKeys;
import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.logistics.trains.management.edgePoint.EdgeData;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.SignalBoundary;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.SignalEdgeGroup;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.TrackEdgePoint;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.outliner.Outliner;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TrackGraphVisualizer {

	public static void visualiseSignalEdgeGroups(TrackGraph graph) {
		Minecraft mc = Minecraft.getInstance();
		Entity cameraEntity = mc.cameraEntity;
		if (cameraEntity == null)
			return;
		AABB box = graph.getBounds(mc.level).box;
		if (box == null || !box.intersects(cameraEntity.getBoundingBox()
			.inflate(50)))
			return;

		Vec3 camera = cameraEntity.getEyePosition();
		Outliner outliner = CreateClient.OUTLINER;
		boolean ctrl = false; // AllKeys.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL);
		Map<UUID, SignalEdgeGroup> allGroups = Create.RAILWAYS.sided(null).signalEdgeGroups;
		float width = 1 / 8f;

		for (Entry<TrackNodeLocation, TrackNode> nodeEntry : graph.nodes.entrySet()) {
			TrackNodeLocation nodeLocation = nodeEntry.getKey();
			TrackNode node = nodeEntry.getValue();
			if (nodeLocation == null)
				continue;

			Vec3 location = nodeLocation.getLocation();
			if (location.distanceTo(camera) > 50)
				continue;
			if (!mc.level.dimension()
				.equals(nodeLocation.dimension))
				continue;

			Map<TrackNode, TrackEdge> map = graph.connectionsByNode.get(node);
			if (map == null)
				continue;

			int hashCode = node.hashCode();
			for (Entry<TrackNode, TrackEdge> entry : map.entrySet()) {
				TrackNode other = entry.getKey();
				TrackEdge edge = entry.getValue();
				EdgeData signalData = edge.getEdgeData();

				if (!edge.node1.location.dimension.equals(edge.node2.location.dimension))
					continue;
				if (other.hashCode() > hashCode && !ctrl)
					continue;

				Vec3 yOffset = new Vec3(0, (other.hashCode() > hashCode ? 6 : 5) / 64f, 0);
				Vec3 startPoint = edge.getPosition(0);
				Vec3 endPoint = edge.getPosition(1);

				if (!edge.isTurn()) {

					// Straight edge with signal boundaries
					if (signalData.hasSignalBoundaries()) {
						double prev = 0;
						double length = edge.getLength();
						SignalBoundary prevBoundary = null;
						SignalEdgeGroup group = null;

						for (TrackEdgePoint trackEdgePoint : signalData.getPoints()) {
							if (!(trackEdgePoint instanceof SignalBoundary boundary))
								continue;

							prevBoundary = boundary;
							group = allGroups.get(boundary.getGroup(node));

							if (group != null)
								outliner.showLine(Pair.of(boundary, edge),
									edge.getPosition(prev + (prev == 0 ? 0 : 1 / 16f / length))
										.add(yOffset),
									edge.getPosition((prev = boundary.getLocationOn(edge) / length) - 1 / 16f / length)
										.add(yOffset))
									.colored(group.color.get())
									.lineWidth(width);

						}

						if (prevBoundary != null) {
							group = allGroups.get(prevBoundary.getGroup(other));
							if (group != null)
								outliner.showLine(edge, edge.getPosition(prev + 1 / 16f / length)
									.add(yOffset), endPoint.add(yOffset))
									.colored(group.color.get())
									.lineWidth(width);
							continue;
						}
					}

					// Straight edge, no signal boundaries
					UUID singleGroup = signalData.getEffectiveEdgeGroupId(graph);
					SignalEdgeGroup singleEdgeGroup = singleGroup == null ? null : allGroups.get(singleGroup);
					if (singleEdgeGroup == null)
						continue;
					outliner.showLine(edge, startPoint.add(yOffset), endPoint.add(yOffset))
						.colored(singleEdgeGroup.color.get())
						.lineWidth(width);

				} else {

					// Bezier edge with signal boundaries
					if (signalData.hasSignalBoundaries()) {
						Iterator<TrackEdgePoint> points = signalData.getPoints()
							.iterator();
						SignalBoundary currentBoundary = null;
						double currentBoundaryPosition = 0;
						while (points.hasNext()) {
							TrackEdgePoint next = points.next();
							if (!(next instanceof SignalBoundary signal))
								continue;
							currentBoundary = signal;
							currentBoundaryPosition = signal.getLocationOn(edge);
							break;
						}

						if (currentBoundary == null)
							continue;
						UUID initialGroupId = currentBoundary.getGroup(node);
						if (initialGroupId == null)
							continue;
						SignalEdgeGroup initialGroup = allGroups.get(initialGroupId);
						if (initialGroup == null)
							continue;

						Color currentColour = initialGroup.color.get();
						Vec3 previous = null;
						BezierConnection turn = edge.getTurn();

						for (int i = 0; i <= turn.getSegmentCount(); i++) {
							double f = i * 1f / turn.getSegmentCount();
							double position = f * turn.getLength();
							Vec3 current = edge.getPosition(f);

							if (previous != null) {
								if (currentBoundary != null && position > currentBoundaryPosition) {
									current = edge.getPosition((currentBoundaryPosition - width) / turn.getLength());
									outliner
										.showLine(Pair.of(edge, previous), previous.add(yOffset), current.add(yOffset))
										.colored(currentColour)
										.lineWidth(width);
									current = edge.getPosition((currentBoundaryPosition + width) / turn.getLength());
									previous = current;
									UUID newId = currentBoundary.getGroup(other);
									if (newId != null && allGroups.containsKey(newId))
										currentColour = allGroups.get(newId).color.get();

									currentBoundary = null;
									while (points.hasNext()) {
										TrackEdgePoint next = points.next();
										if (!(next instanceof SignalBoundary signal))
											continue;
										currentBoundary = signal;
										currentBoundaryPosition = signal.getLocationOn(edge);
										break;
									}
								}

								outliner.showLine(Pair.of(edge, previous), previous.add(yOffset), current.add(yOffset))
									.colored(currentColour)
									.lineWidth(width);
							}

							previous = current;
						}
					}

					// Bezier edge, no signal boundaries
					UUID singleGroup = signalData.getEffectiveEdgeGroupId(graph);
					SignalEdgeGroup singleEdgeGroup = singleGroup == null ? null : allGroups.get(singleGroup);
					if (singleEdgeGroup == null)
						continue;
					Vec3 previous = null;
					BezierConnection turn = edge.getTurn();
					for (int i = 0; i <= turn.getSegmentCount(); i++) {
						Vec3 current = edge.getPosition(i * 1f / turn.getSegmentCount());
						if (previous != null)
							outliner.showLine(Pair.of(edge, previous), previous.add(yOffset), current.add(yOffset))
								.colored(singleEdgeGroup.color.get())
								.lineWidth(width);
						previous = current;
					}
				}
			}
		}
	}

	public static void debugViewGraph(TrackGraph graph) {
		Minecraft mc = Minecraft.getInstance();
		Entity cameraEntity = mc.cameraEntity;
		if (cameraEntity == null)
			return;
		AABB box = graph.getBounds(mc.level).box;
		if (box == null || !box.intersects(cameraEntity.getBoundingBox()
			.inflate(50)))
			return;

		Vec3 camera = cameraEntity.getEyePosition();
		for (Entry<TrackNodeLocation, TrackNode> nodeEntry : graph.nodes.entrySet()) {
			TrackNodeLocation nodeLocation = nodeEntry.getKey();
			TrackNode node = nodeEntry.getValue();
			if (nodeLocation == null)
				continue;

			Vec3 location = nodeLocation.getLocation();
			if (location.distanceTo(camera) > 50)
				continue;
			if (!mc.level.dimension()
				.equals(nodeLocation.dimension))
				continue;

			Vec3 yOffset = new Vec3(0, 3 / 16f, 0);
			Vec3 v1 = location.add(yOffset);
			Vec3 v2 = v1.add(node.normal.scale(3 / 16f));
			CreateClient.OUTLINER.showLine(Integer.valueOf(node.netId), v1, v2)
				.colored(Color.mixColors(Color.WHITE, graph.color, 1))
				.lineWidth(1 / 8f);

			Map<TrackNode, TrackEdge> map = graph.connectionsByNode.get(node);
			if (map == null)
				continue;

			int hashCode = node.hashCode();
			for (Entry<TrackNode, TrackEdge> entry : map.entrySet()) {
				TrackNode other = entry.getKey();
				TrackEdge edge = entry.getValue();

				if (!edge.node1.location.dimension.equals(edge.node2.location.dimension)) {
					v1 = location.add(yOffset);
					v2 = v1.add(node.normal.scale(3 / 16f));
					CreateClient.OUTLINER.showLine(Integer.valueOf(node.netId), v1, v2)
						.colored(Color.mixColors(Color.WHITE, graph.color, 1))
						.lineWidth(1 / 4f);
					continue;
				}
				if (other.hashCode() > hashCode && !AllKeys.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL))
					continue;

				yOffset = new Vec3(0, (other.hashCode() > hashCode ? 6 : 4) / 16f, 0);
				if (!edge.isTurn()) {
					CreateClient.OUTLINER.showLine(edge, edge.getPosition(0)
						.add(yOffset),
						edge.getPosition(1)
							.add(yOffset))
						.colored(graph.color)
						.lineWidth(1 / 16f);
					continue;
				}

				Vec3 previous = null;
				BezierConnection turn = edge.getTurn();
				for (int i = 0; i <= turn.getSegmentCount(); i++) {
					Vec3 current = edge.getPosition(i * 1f / turn.getSegmentCount());
					if (previous != null)
						CreateClient.OUTLINER
							.showLine(Pair.of(edge, previous), previous.add(yOffset), current.add(yOffset))
							.colored(graph.color)
							.lineWidth(1 / 16f);
					previous = current;
				}
			}
		}
	}

}
