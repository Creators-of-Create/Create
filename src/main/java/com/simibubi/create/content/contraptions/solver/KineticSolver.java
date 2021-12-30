package com.simibubi.create.content.contraptions.solver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.utility.WorldAttached;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class KineticSolver {

	private static final WorldAttached<KineticSolver> SOLVERS = new WorldAttached<>($ -> new KineticSolver());

	public static KineticSolver getSolver(Level level) {
		return SOLVERS.get(level);
	}

	private final Map<BlockPos, KineticNode> nodes = new HashMap<>();

	public void addNode(KineticTileEntity entity) {
		removeNode(entity);
		KineticNode node = new KineticNode(entity, this::getNode);
		nodes.put(entity.getBlockPos(), node);
		node.onAdded();
	}

	public void updateNode(KineticTileEntity entity) {
		KineticNode node = nodes.get(entity.getBlockPos());

		if (!node.getConnections().equals(entity.getConnections())) {
			// connections changed, so things could've been disconnected
			removeNode(entity);
			addNode(entity);
		} else {
			// connections are the same, so just update in case other properties changed
			node.onUpdated();
		}
	}

	protected Optional<KineticNode> getNode(BlockPos pos) {
		return Optional.ofNullable(nodes.get(pos));
	}

	public void removeNode(KineticTileEntity entity) {
		KineticNode node = nodes.remove(entity.getBlockPos());
		if (node != null) node.onRemoved();
	}

	public void tick() {
		Set<KineticNetwork> visited = new HashSet<>();
		List<KineticNetwork> frontier = new LinkedList<>();

		Set<KineticNetwork> networks = nodes.values().stream().map(KineticNode::getNetwork).collect(Collectors.toSet());
		for (KineticNetwork network : networks) {
			frontier.add(network);
			while (!frontier.isEmpty()) {
				KineticNetwork cur = frontier.remove(0);
				if (visited.contains(cur)) continue;
				visited.add(cur);
				frontier.addAll(cur.tick());
			}
		}
	}

}
