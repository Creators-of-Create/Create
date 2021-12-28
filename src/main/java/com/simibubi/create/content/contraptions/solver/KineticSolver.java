package com.simibubi.create.content.contraptions.solver;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
		KineticNodeState state = entity.getKineticNodeState();

		if (!node.getConnections().equals(state.getConnections())) {
			// connections changed, so things could've been disconnected
			removeNode(entity);
			addNode(entity);
		} else {
			// connections are the same, so just set speed in case it changed
			node.setGeneratedSpeed(state.getGeneratedSpeed());
		}
	}

	protected Optional<KineticNode> getNode(BlockPos pos) {
		return Optional.ofNullable(nodes.get(pos));
	}

	public void removeNode(KineticTileEntity entity) {
		KineticNode node = nodes.remove(entity.getBlockPos());
		if (node != null) node.onRemoved();
	}

	public void flushChangedSpeeds() {
		nodes.values().forEach(KineticNode::flushChangedSpeed);
	}
}
