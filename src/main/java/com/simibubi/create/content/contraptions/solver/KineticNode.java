package com.simibubi.create.content.contraptions.solver;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class KineticNode {

	private final Function<BlockPos, Optional<KineticNode>> nodeAccessor;
	private final KineticTileEntity entity;

	private @Nullable KineticNode source;
	private KineticNetwork network;
	private float speedRatio = 1;

	private final KineticConnections connections;
	private float generatedSpeed;

	private float speedCur;
	private float speedNext;

	public KineticNode(KineticTileEntity entity, Function<BlockPos, Optional<KineticNode>> nodeAccessor) {
		this.nodeAccessor = nodeAccessor;
		this.entity = entity;

		KineticNodeState state = entity.getKineticNodeState();
		this.connections = state.getConnections();
		this.generatedSpeed = state.getGeneratedSpeed();

		this.network = new KineticNetwork(this);
	}

	public KineticConnections getConnections() {
		return connections;
	}

	/**
	 * @return a map where the keys are every node with a compatible connection to this node, and the values are the
	 * speed ratios of those connections
	 */
	public Map<KineticNode, Float> getActiveConnections() {
		return connections.getDirections().stream()
				.map(d -> nodeAccessor.apply(entity.getBlockPos().offset(d))
						.map(n -> connections.checkConnection(n.connections, d)
								.map(r -> Pair.of(n, r))))
				.flatMap(Optional::stream)
				.flatMap(Optional::stream)
				.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
	}

	public float getGeneratedSpeed() {
		return generatedSpeed;
	}

	public float getGeneratedSpeedAtRoot() {
		return generatedSpeed / speedRatio;
	}

	public boolean isGenerator() {
		return generatedSpeed != 0;
	}

	public void setGeneratedSpeed(float newSpeed) {
		if (Mth.equal(generatedSpeed, newSpeed)) return;
		generatedSpeed = newSpeed;
		network.updateMember(this);
		if (network.recalculateSpeed(this, false).isContradiction())
			onPopBlock();
	}

	private void setNetwork(KineticNetwork network) {
		this.network.removeMember(this);
		this.network = network;
		network.addMember(this);
	}

	private void setSource(KineticNode from, float ratio) {
		source = from;
		speedRatio = from.speedRatio * ratio;
		setNetwork(from.network);
	}

	public void onAdded() {
		getActiveConnections()
				.keySet()
				.stream()
				.findAny()
				.ifPresent(n -> {
					if (n.propagateSource(this).isContradiction())
						onPopBlock();
				});
	}

	/**
	 * Propagates this node's source and network to any connected nodes that aren't yet part of the same network, then
	 * repeats this recursively with the connected nodes in a breadth-first order.
	 * @param checkRoot Node to start searching from when looking for nodes that started speeding because of this call
	 * @return			whether or not this propagation caused a contradiction in the kinetic network
	 */
	private SolveResult propagateSource(KineticNode checkRoot) {
		List<KineticNode> frontier = new LinkedList<>();
		frontier.add(this);

		while (!frontier.isEmpty()) {
			KineticNode cur = frontier.remove(0);
			for (Map.Entry<KineticNode, Float> entry : cur.getActiveConnections().entrySet()) {
				KineticNode next = entry.getKey();
				float ratio = entry.getValue();

				if (next == cur.source) continue;
				if (next.network == network) {
					if (!Mth.equal(next.speedRatio, cur.speedRatio * ratio)) {
						// we found a cycle with conflicting speed ratios
						network.markConflictingCycle(cur, next);
					}
					continue;
				}

				next.setSource(cur, ratio);
				frontier.add(next);
			}
		}

		return network.recalculateSpeed(checkRoot, true);
	}

	public void onRemoved() {
		network.removeMember(this);
		for (KineticNode neighbor : getActiveConnections().keySet()) {
			if (neighbor.source != this) continue;
			neighbor.rerootHere();
		}
		network.recalculateSpeed(null, false);
	}

	private void rerootHere() {
		source = null;
		speedRatio = 1;
		setNetwork(new KineticNetwork(this));
		if (tryUpdateSpeed().isOk()) {
			propagateSource(this);
		} else {
			onPopBlock();
		}
	}

	/**
	 * Updates the speed of this node based on its network's root speed and its own speed ratio.
	 * @return CONTRADICTION if the node's new speed exceeds the maximum value, and OK otherwise
	 */
	protected SolveResult tryUpdateSpeed() {
		speedNext = network.getRootSpeed() * speedRatio;
		if (Math.abs(speedNext) > AllConfigs.SERVER.kinetics.maxRotationSpeed.get())
			return SolveResult.CONTRADICTION;
		return SolveResult.OK;
	}

	public void flushChangedSpeed() {
		if (speedCur != speedNext) {
			speedCur = speedNext;
			// TODO: update entity's speed
			System.out.printf("Set speed of %s to %f\n", this, speedNext);
		}
	}

	public void onPopBlock() {
		// this should cause the node to get removed from the solver and lead to onRemoved() being called
		entity.getLevel().destroyBlock(entity.getBlockPos(), true);
	}

	public boolean isSourceOf(KineticNode other) {
		return other.source == this;
	}

}
