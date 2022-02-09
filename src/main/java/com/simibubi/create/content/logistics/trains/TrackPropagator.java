package com.simibubi.create.content.logistics.trains;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.TrackNodeLocation.DiscoveredLocation;
import com.simibubi.create.content.logistics.trains.track.TrackBlock;
import com.simibubi.create.content.logistics.trains.track.TrackBlock.TrackShape;
import com.simibubi.create.content.logistics.trains.track.TrackTileEntity;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TrackPropagator {

	static class FrontierEntry {
		BlockPos prevPos;
		DiscoveredLocation prevNode;
		BlockPos currentPos;
		DiscoveredLocation currentNode;
		DiscoveredLocation parentNode;

		public FrontierEntry(BlockPos previousPos, BlockPos pos, DiscoveredLocation location) {
			this(null, previousPos, null, pos, location);
		}

		public FrontierEntry(DiscoveredLocation parent, BlockPos previousPos, DiscoveredLocation previousNode,
			BlockPos pos, DiscoveredLocation location) {
			parentNode = parent;
			prevPos = previousPos;
			prevNode = previousNode;
			currentPos = pos;
			currentNode = location;
		}

	}

	public static void onRailRemoved(LevelAccessor reader, BlockPos pos, BlockState state) {
		List<Pair<BlockPos, DiscoveredLocation>> ends = getEnds(reader, pos, state, null, false);
		TrackGraph foundGraph = null;
		GlobalRailwayManager manager = Create.RAILWAYS;
		TrackGraphSync sync = manager.sync;

		for (Pair<BlockPos, DiscoveredLocation> removedEnd : ends) {
			DiscoveredLocation removedLocation = removedEnd.getSecond();
			if (foundGraph == null)
				foundGraph = manager.getGraph(reader, removedLocation);
			if (foundGraph != null) {
				TrackNode removedNode = foundGraph.locateNode(removedLocation);
				if (removedNode != null) {
					foundGraph.removeNode(reader, removedLocation);
					sync.nodeRemoved(foundGraph, removedNode);
				}
			}
		}

		if (foundGraph != null && foundGraph.isEmpty()) {
			manager.removeGraph(foundGraph);
			sync.graphRemoved(foundGraph);
		}

		Set<TrackGraph> toUpdate = new HashSet<>();
		for (Pair<BlockPos, DiscoveredLocation> removedEnd : ends) {
			BlockPos adjPos = removedEnd.getFirst();
			BlockState adjState = reader.getBlockState(adjPos);

			if (!getEnds(reader, adjPos, adjState, removedEnd.getSecond(), true).isEmpty())
				toUpdate.add(onRailAdded(reader, adjPos, adjState));
		}

		for (TrackGraph railGraph : toUpdate)
			manager.updateSplitGraph(railGraph);

		manager.markTracksDirty();
	}

	public static TrackGraph onRailAdded(LevelAccessor reader, BlockPos pos, BlockState state) {
		// 1. Remove all immediately reachable node locations

		GlobalRailwayManager manager = Create.RAILWAYS;
		TrackGraphSync sync = manager.sync;
		List<FrontierEntry> frontier = new ArrayList<>();
		Set<DiscoveredLocation> visited = new HashSet<>();
		Set<TrackGraph> connectedGraphs = new HashSet<>();
		addInitialEndsOf(reader, pos, state, frontier, false);

		int emergencyExit = 1000;
		while (!frontier.isEmpty()) {
			if (emergencyExit-- == 0)
				break;

			FrontierEntry entry = frontier.remove(0);
			List<Pair<BlockPos, DiscoveredLocation>> ends = findReachableEnds(reader, entry);
			TrackGraph graph = manager.getGraph(reader, entry.currentNode);
			if (graph != null) {
				TrackNode node = graph.locateNode(entry.currentNode);
				graph.removeNode(reader, entry.currentNode);
				sync.nodeRemoved(graph, node);
				connectedGraphs.add(graph);
				continue;
			}

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
			manager.removeGraph(railGraph);
			sync.graphRemoved(railGraph);
			iterator.remove();
		}

		// Merge graphs if more than 1
		if (connectedGraphs.size() > 1) {
			for (TrackGraph other : connectedGraphs)
				if (graph == null)
					graph = other;
				else {
					other.transferAll(graph);
					manager.removeGraph(other);
					sync.graphRemoved(other);
				}
		} else if (connectedGraphs.size() == 1) {
			graph = connectedGraphs.stream()
				.findFirst()
				.get();
		} else
			manager.putGraph(graph = new TrackGraph());

		DiscoveredLocation startNode = null;
		List<BlockPos> startPositions = new ArrayList<>();

		// 2. Find the first graph node candidate nearby

		addInitialEndsOf(reader, pos, state, frontier, true);

		emergencyExit = 1000;
		while (!frontier.isEmpty()) {
			if (emergencyExit-- == 0)
				break;

			FrontierEntry entry = frontier.remove(0);

//			CreateClient.OUTLINER
//				.showAABB(entry.currentNode, new AABB(entry.currentNode.getLocation(), entry.currentNode.getLocation()
//					.add(0, 2, 0)), 120)
//				.colored(Color.GREEN)
//				.lineWidth(1 / 16f);
//			CreateClient.OUTLINER.showAABB(entry.currentPos, new AABB(entry.currentPos).contract(0, 1, 0), 120)
//				.colored(0x7777ff)
//				.lineWidth(1 / 16f);
//			if (entry.prevPos != null) {
//				CreateClient.OUTLINER.showAABB(entry.prevPos, new AABB(entry.prevPos).contract(0, 1, 0), 120)
//					.colored(0x3333aa)
//					.lineWidth(1 / 16f);
//			}

			List<Pair<BlockPos, DiscoveredLocation>> ends = findReachableEnds(reader, entry);
			if (isValidGraphNodeLocation(entry.currentNode, ends)) {
				startNode = entry.currentNode;
				startPositions.add(entry.prevPos);
				startPositions.add(entry.currentPos);
				break;
			}

			continueSearch(frontier, visited, entry, ends);
		}

		frontier.clear();
		if (graph.createNode(startNode))
			sync.nodeAdded(graph, graph.locateNode(startNode));

//		CreateClient.OUTLINER.showAABB(graph, new AABB(startNode.getLocation(), startNode.getLocation()
//			.add(0, 2, 0)), 20)
//			.lineWidth(1 / 32f);

		for (BlockPos position : startPositions)
			frontier.add(new FrontierEntry(startNode, null, null, position, startNode));

		// 3. Build up the graph via all connected nodes

		emergencyExit = 1000;
		while (!frontier.isEmpty()) {
			if (emergencyExit-- == 0)
				break;

			FrontierEntry entry = frontier.remove(0);
			DiscoveredLocation parentNode = entry.parentNode;
			List<Pair<BlockPos, DiscoveredLocation>> ends = findReachableEnds(reader, entry);

			if (isValidGraphNodeLocation(entry.currentNode, ends) && entry.currentNode != startNode) {
				boolean nodeIsNew = graph.createNode(entry.currentNode);
				if (nodeIsNew)
					sync.nodeAdded(graph, graph.locateNode(entry.currentNode));
				graph.connectNodes(parentNode, entry.currentNode, new TrackEdge(entry.currentNode.getTurn()));
				parentNode = entry.currentNode;
				if (!nodeIsNew)
					continue;
			}

			continueSearchWithParent(frontier, entry, parentNode, ends);
		}

		manager.markTracksDirty();
		return graph;
	}

	private static void addInitialEndsOf(LevelAccessor reader, BlockPos pos, BlockState state,
		List<FrontierEntry> frontier, boolean ignoreTurns) {
		for (Pair<BlockPos, DiscoveredLocation> initial : getEnds(reader, pos, state, null, ignoreTurns))
			frontier.add(new FrontierEntry(initial.getFirst(), pos, initial.getSecond()));
	}

	private static List<Pair<BlockPos, DiscoveredLocation>> findReachableEnds(LevelAccessor reader,
		FrontierEntry entry) {
		BlockState currentState = reader.getBlockState(entry.currentPos);
		List<Pair<BlockPos, DiscoveredLocation>> ends = new ArrayList<>();

		if (entry.prevNode != null) {
			BlockPos prevPos = entry.prevPos;

			// PrevPos correction after a turn
			if (entry.currentNode.connectedViaTurn()) {
				boolean slope = false;
				if (currentState.getBlock()instanceof ITrackBlock track)
					slope = track.isSlope(reader, entry.currentPos, currentState);
				BlockPos offset = new BlockPos(VecHelper.getCenterOf(entry.currentPos)
					.subtract(entry.currentNode.getLocation()
						.add(0, slope ? 0 : .5f, 0))
					.scale(-2));
				prevPos = entry.currentPos.offset(offset);
			}

			for (Pair<BlockPos, DiscoveredLocation> pair : getEnds(reader, prevPos, reader.getBlockState(prevPos),
				entry.currentNode, false))
				if (!pair.getSecond()
					.equals(entry.prevNode))
					ends.add(pair);
		}

		ends.addAll(getEnds(reader, entry.currentPos, currentState, entry.currentNode, false));
		return ends;
	}

	private static void continueSearch(List<FrontierEntry> frontier, Set<DiscoveredLocation> visited,
		FrontierEntry entry, List<Pair<BlockPos, DiscoveredLocation>> ends) {
		for (Pair<BlockPos, DiscoveredLocation> pair : ends)
			if (visited.add(pair.getSecond()))
				frontier.add(
					new FrontierEntry(null, entry.currentPos, entry.currentNode, pair.getFirst(), pair.getSecond()));
	}

	private static void continueSearchWithParent(List<FrontierEntry> frontier, FrontierEntry entry,
		DiscoveredLocation parentNode, List<Pair<BlockPos, DiscoveredLocation>> ends) {
		for (Pair<BlockPos, DiscoveredLocation> pair : ends)
			frontier.add(
				new FrontierEntry(parentNode, entry.currentPos, entry.currentNode, pair.getFirst(), pair.getSecond()));
	}

	public static boolean isValidGraphNodeLocation(DiscoveredLocation location,
		List<Pair<BlockPos, DiscoveredLocation>> next) {
		if (next.size() != 1)
			return true;
		if (location.connectedViaTurn())
			return true;

		DiscoveredLocation nextLocation = next.iterator()
			.next()
			.getSecond();

		if (nextLocation.connectedViaTurn())
			return true;

		Vec3 vec = location.getLocation();
		boolean centeredX = !Mth.equal(vec.x, Math.round(vec.x));
		boolean centeredZ = !Mth.equal(vec.z, Math.round(vec.z));
		if (centeredX && !centeredZ)
			return ((int) Math.round(vec.z)) % 16 == 0;
		return ((int) Math.round(vec.x)) % 16 == 0;
	}

	// TODO ITrackBlock
	public static List<Pair<BlockPos, DiscoveredLocation>> getEnds(LevelReader reader, BlockPos pos, BlockState state,
		@Nullable DiscoveredLocation fromEnd, boolean ignoreTurns) {
		Vec3 center = VecHelper.getCenterOf(pos);
		List<Pair<BlockPos, DiscoveredLocation>> list = new ArrayList<>();

		if (!(state.getBlock() instanceof TrackBlock))
			return list;

		BlockEntity blockEntity = reader.getBlockEntity(pos);
		if (state.getValue(TrackBlock.HAS_TURN) && blockEntity instanceof TrackTileEntity && !ignoreTurns) {
			TrackTileEntity trackTileEntity = (TrackTileEntity) blockEntity;
			trackTileEntity.getConnections()
				.forEach(map -> map.forEach((connectedPos, bc) -> addToSet(fromEnd, list,
					(d, b) -> d == 1 ? Vec3.atLowerCornerOf(bc.tePositions.get(b)) : bc.starts.get(b), bc.normals::get,
					bc)));
		}

		TrackShape shape = state.getValue(TrackBlock.SHAPE);
		if (shape != TrackShape.NONE)
			shape.getAxes()
				.forEach(axis -> addToSet(fromEnd, list, (d, b) -> axis.scale(b ? d : -d)
					.add(center)
					.add(0, axis.y == 0 ? -.5 : 0, 0), b -> shape.getNormal(), null));

		return list;
	}

	private static void addToSet(DiscoveredLocation fromEnd, List<Pair<BlockPos, DiscoveredLocation>> list,
		BiFunction<Double, Boolean, Vec3> offsetFactory, Function<Boolean, Vec3> normalFactory,
		BezierConnection viaTurn) {

		DiscoveredLocation firstLocation = new DiscoveredLocation(offsetFactory.apply(0.5d, true));
		DiscoveredLocation secondLocation = new DiscoveredLocation(offsetFactory.apply(0.5d, false));

		Pair<BlockPos, DiscoveredLocation> firstNode =
			Pair.of(new BlockPos(offsetFactory.apply(1.0d, true)), firstLocation.viaTurn(viaTurn)
				.withNormal(normalFactory.apply(true)));
		Pair<BlockPos, DiscoveredLocation> secondNode =
			Pair.of(new BlockPos(offsetFactory.apply(1.0d, false)), secondLocation.viaTurn(viaTurn)
				.withNormal(normalFactory.apply(false)));

		boolean skipFirst = false;
		boolean skipSecond = false;

		if (fromEnd != null) {
			boolean equalsFirst = firstNode.getSecond()
				.equals(fromEnd);
			boolean equalsSecond = secondNode.getSecond()
				.equals(fromEnd);

			// not reachable from this end, crossover rail
			if (!equalsFirst && !equalsSecond)
				return;

			if (equalsFirst)
				skipFirst = true;
			if (equalsSecond)
				skipSecond = true;
		}

		if (!skipFirst)
			list.add(firstNode);
		if (!skipSecond)
			list.add(secondNode);
	}

}
