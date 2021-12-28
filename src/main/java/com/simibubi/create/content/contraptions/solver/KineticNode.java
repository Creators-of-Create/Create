package com.simibubi.create.content.contraptions.solver;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.core.BlockPos;

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
		onSpeedUpdated();
	}

	public KineticConnections getConnections() {
		return connections;
	}

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
		generatedSpeed = newSpeed;
		network.updateMember(this);
		if (network.recalculateSpeed().isContradiction())
			onPopBlock();
	}

	private void setNetwork(KineticNetwork network) {
		this.network.removeMember(this);
		this.network = network;
		network.addMember(this);
		onSpeedUpdated();
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
					if (n.propagateSource().isContradiction())
						onPopBlock();
				});
	}

	public void onRemoved() {
		network.removeMember(this);
		for (KineticNode neighbor : getActiveConnections().keySet()) {
			if (neighbor.source != this) continue;
			neighbor.rerootHere();
		}
		network.recalculateSpeed();
	}

	private SolveResult propagateSource() {
		List<KineticNode> frontier = new LinkedList<>();
		frontier.add(this);

		while (!frontier.isEmpty()) {
			KineticNode cur = frontier.remove(0);
			for (Map.Entry<KineticNode, Float> entry : cur.getActiveConnections().entrySet()) {
				KineticNode next = entry.getKey();
				float ratio = entry.getValue();
				if (next == cur.source) continue;
				if (next.network == network) {
					if (next.speedRatio != cur.speedRatio * ratio) {
						network.markConflictingCycle(cur, next);
					}
					continue;
				}
				next.setSource(cur, ratio);
				frontier.add(next);
			}
		}

		return network.recalculateSpeed();
	}

	private void rerootHere() {
		source = null;
		speedRatio = 1;
		setNetwork(new KineticNetwork(this));
		propagateSource();
	}

	public void onSpeedUpdated() {
		speedNext = network.getRootSpeed() * speedRatio;
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

}
