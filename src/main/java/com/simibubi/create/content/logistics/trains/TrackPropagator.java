package com.simibubi.create.content.logistics.trains;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.simibubi.create.Create;
import com.simibubi.create.api.event.TrackGraphMergeEvent;
import com.simibubi.create.content.logistics.trains.TrackNodeLocation.DiscoveredLocation;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.SignalPropagator;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

public class TrackPropagator {

	static class FrontierEntry {
		DiscoveredLocation prevNode;
		DiscoveredLocation currentNode;
		DiscoveredLocation parentNode;

		public FrontierEntry(DiscoveredLocation parent, DiscoveredLocation previousNode, DiscoveredLocation location) {
			parentNode = parent;
			prevNode = previousNode;
			currentNode = location;
		}
	}

	public static void onRailRemoved(LevelAccessor reader, BlockPos pos, BlockState state) {
		if (!(state.getBlock() instanceof ITrackBlock track))
			return;

		Collection<DiscoveredLocation> ends = track.getConnected(reader, pos, state, false, null);
		GlobalRailwayManager manager = Create.RAILWAYS;
		TrackGraphSync sync = manager.sync;

		// 1. Remove any nodes this rail was part of

		for (DiscoveredLocation removedLocation : ends) {
			List<TrackGraph> intersecting = manager.getGraphs(reader, removedLocation);
			for (TrackGraph foundGraph : intersecting) {
				TrackNode removedNode = foundGraph.locateNode(removedLocation);
				if (removedNode == null)
					continue;
				foundGraph.removeNode(reader, removedLocation);
				sync.nodeRemoved(foundGraph, removedNode);
				if (!foundGraph.isEmpty())
					continue;
				manager.removeGraphAndGroup(foundGraph);
				sync.graphRemoved(foundGraph);
			}
		}

		Set<BlockPos> positionsToUpdate = new HashSet<>();
		for (DiscoveredLocation removedEnd : ends)
			positionsToUpdate.addAll(removedEnd.allAdjacent());

		// 2. Re-run railAdded for any track that was disconnected from this track

		Set<TrackGraph> toUpdate = new HashSet<>();
		for (BlockPos blockPos : positionsToUpdate)
			if (!blockPos.equals(pos)) {
				TrackGraph onRailAdded = onRailAdded(reader, blockPos, reader.getBlockState(blockPos));
				if (onRailAdded != null)
					toUpdate.add(onRailAdded);
			}

		// 3. Ensure any affected graph gets checked for segmentation

		for (TrackGraph railGraph : toUpdate)
			manager.updateSplitGraph(reader, railGraph);

		manager.markTracksDirty();
	}

	public static TrackGraph onRailAdded(LevelAccessor reader, BlockPos pos, BlockState state) {
		if (!(state.getBlock()instanceof ITrackBlock track))
			return null;

		// 1. Remove all immediately reachable node locations

		GlobalRailwayManager manager = Create.RAILWAYS;
		TrackGraphSync sync = manager.sync;
		List<FrontierEntry> frontier = new ArrayList<>();
		Set<DiscoveredLocation> visited = new HashSet<>();
		Set<TrackGraph> connectedGraphs = new HashSet<>();
		addInitialEndsOf(reader, pos, state, track, frontier, false);

		int emergencyExit = 1000;
		while (!frontier.isEmpty()) {
			if (emergencyExit-- == 0)
				break;

			FrontierEntry entry = frontier.remove(0);
			List<TrackGraph> intersecting = manager.getGraphs(reader, entry.currentNode);
			for (TrackGraph graph : intersecting) {
				TrackNode node = graph.locateNode(entry.currentNode);
				graph.removeNode(reader, entry.currentNode);
				sync.nodeRemoved(graph, node);
				connectedGraphs.add(graph);
				continue;
			}

			if (!intersecting.isEmpty())
				continue;

			Collection<DiscoveredLocation> ends = ITrackBlock.walkConnectedTracks(reader, entry.currentNode, false);
			if (entry.prevNode != null)
				ends.remove(entry.prevNode);
			continueSearch(frontier, visited, entry, ends);
		}

		frontier.clear();
		visited.clear();
		TrackGraph graph = null;

		// Remove empty graphs
		for (Iterator<TrackGraph> iterator = connectedGraphs.iterator(); iterator.hasNext();) {
			TrackGraph railGraph = iterator.next();
			if (!railGraph.isEmpty() || connectedGraphs.size() == 1)
				continue;
			manager.removeGraphAndGroup(railGraph);
			sync.graphRemoved(railGraph);
			iterator.remove();
		}

		// Merge graphs if more than 1
		if (connectedGraphs.size() > 1) {
			for (TrackGraph other : connectedGraphs)
				if (graph == null)
					graph = other;
				else {
					MinecraftForge.EVENT_BUS.post(new TrackGraphMergeEvent(other, graph));
					other.transferAll(graph);
					manager.removeGraphAndGroup(other);
					sync.graphRemoved(other);
				}
		} else if (connectedGraphs.size() == 1) {
			graph = connectedGraphs.stream()
				.findFirst()
				.get();
		} else
			manager.putGraphWithDefaultGroup(graph = new TrackGraph());

		DiscoveredLocation startNode = null;

		// 2. Find the first graph node candidate nearby

		addInitialEndsOf(reader, pos, state, track, frontier, true);

		emergencyExit = 1000;
		while (!frontier.isEmpty()) {
			if (emergencyExit-- == 0)
				break;

			FrontierEntry entry = frontier.remove(0);
			Collection<DiscoveredLocation> ends = ITrackBlock.walkConnectedTracks(reader, entry.currentNode, false);
			boolean first = entry.prevNode == null;
			if (!first)
				ends.remove(entry.prevNode);
			if (isValidGraphNodeLocation(entry.currentNode, ends, first)) {
				startNode = entry.currentNode;
				break;
			}

			continueSearch(frontier, visited, entry, ends);
		}

		frontier.clear();
		Set<TrackNode> addedNodes = new HashSet<>();
		graph.createNodeIfAbsent(startNode);
		frontier.add(new FrontierEntry(startNode, null, startNode));

		// 3. Build up the graph via all connected nodes

		emergencyExit = 1000;
		while (!frontier.isEmpty()) {
			if (emergencyExit-- == 0)
				break;

			FrontierEntry entry = frontier.remove(0);
			DiscoveredLocation parentNode = entry.parentNode;
			Collection<DiscoveredLocation> ends = ITrackBlock.walkConnectedTracks(reader, entry.currentNode, false);
			boolean first = entry.prevNode == null;
			if (!first)
				ends.remove(entry.prevNode);

			if (isValidGraphNodeLocation(entry.currentNode, ends, first) && entry.currentNode != startNode) {
				boolean nodeIsNew = graph.createNodeIfAbsent(entry.currentNode);
				graph.connectNodes(reader, parentNode, entry.currentNode, entry.currentNode.getTurn());
				addedNodes.add(graph.locateNode(entry.currentNode));
				parentNode = entry.currentNode;
				if (!nodeIsNew)
					continue;
			}

			continueSearchWithParent(frontier, entry, parentNode, ends);
		}

		manager.markTracksDirty();
		for (TrackNode trackNode : addedNodes)
			SignalPropagator.notifySignalsOfNewNode(graph, trackNode);
		return graph;
	}

	private static void addInitialEndsOf(LevelAccessor reader, BlockPos pos, BlockState state, ITrackBlock track,
		List<FrontierEntry> frontier, boolean ignoreTurns) {
		for (DiscoveredLocation initial : track.getConnected(reader, pos, state, ignoreTurns, null)) {
			frontier.add(new FrontierEntry(null, null, initial));
		}
	}

	private static void continueSearch(List<FrontierEntry> frontier, Set<DiscoveredLocation> visited,
		FrontierEntry entry, Collection<DiscoveredLocation> ends) {
		for (DiscoveredLocation location : ends)
			if (visited.add(location))
				frontier.add(new FrontierEntry(null, entry.currentNode, location));
	}

	private static void continueSearchWithParent(List<FrontierEntry> frontier, FrontierEntry entry,
		DiscoveredLocation parentNode, Collection<DiscoveredLocation> ends) {
		for (DiscoveredLocation location : ends)
			frontier.add(new FrontierEntry(parentNode, entry.currentNode, location));
	}

	public static boolean isValidGraphNodeLocation(DiscoveredLocation location, Collection<DiscoveredLocation> next,
		boolean first) {
		int size = next.size() - (first ? 1 : 0);
		if (size != 1)
			return true;
		if (location.shouldForceNode())
			return true;
		if (next.stream()
			.anyMatch(DiscoveredLocation::shouldForceNode))
			return true;
		
		Vec3 direction = location.direction;
		if (direction != null && next.stream()
			.anyMatch(dl -> dl.notInLineWith(direction)))
			return true;

		Vec3 vec = location.getLocation();
		boolean centeredX = !Mth.equal(vec.x, Math.round(vec.x));
		boolean centeredZ = !Mth.equal(vec.z, Math.round(vec.z));
		if (centeredX && !centeredZ)
			return ((int) Math.round(vec.z)) % 16 == 0;
		return ((int) Math.round(vec.x)) % 16 == 0;
	}

}
