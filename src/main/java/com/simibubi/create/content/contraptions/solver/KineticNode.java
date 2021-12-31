package com.simibubi.create.content.contraptions.solver;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KineticNode {

	private final Function<BlockPos, Optional<KineticNode>> nodeAccessor;
	private final KineticTileEntity entity;

	private @Nullable KineticNode source;
	private KineticNetwork network;
	private float speedRatio = 1;

	private final KineticConnections connections;
	private float generatedSpeed;
	private float stressCapacity;
	private float stressImpact;

	private float speedCur;
	private float speedNext;

	public KineticNode(KineticTileEntity entity, Function<BlockPos, Optional<KineticNode>> nodeAccessor) {
		this.nodeAccessor = nodeAccessor;
		this.entity = entity;

		this.connections = entity.getConnections();
		this.generatedSpeed = entity.getGeneratedSpeed();
		this.stressImpact = entity.getStressImpact();
		this.stressCapacity = entity.getStressCapacity();

		this.network = new KineticNetwork(this);
	}

	public KineticConnections getConnections() {
		return connections;
	}

	public KineticNetwork getNetwork() {
		return network;
	}

	/**
	 * @return 	a Stream containing a pair for each compatible connection with this node, where the first value is
	 * 			the connecting node and the second value is the speed ratio of the connection
	 */
	public Stream<Pair<KineticNode, Float>> getActiveConnections() {
		return connections.getDirections().stream()
				.map(d -> nodeAccessor.apply(entity.getBlockPos().offset(d))
						.map(n -> connections.checkConnection(n.connections, d)
								.map(r -> Pair.of(n, r))))
				.flatMap(Optional::stream)
				.flatMap(Optional::stream);
	}

	public Iterable<Pair<KineticNode, Float>> getActiveConnectionsList() {
		return getActiveConnections().collect(Collectors.toList());
	}

	public Stream<KineticNetwork> getActiveStressOnlyConnections() {
		return connections.getDirections().stream()
				.map(d -> nodeAccessor.apply(entity.getBlockPos().offset(d))
						.filter(n -> connections.checkStressOnlyConnection(n.connections, d)))
				.flatMap(Optional::stream)
				.map(KineticNode::getNetwork);
	}

	public float getGeneratedSpeedAtRoot() {
		return generatedSpeed / speedRatio;
	}

	public boolean isGenerator() {
		return generatedSpeed != 0;
	}

	public void onUpdated() {
		float generatedSpeedNew = entity.getGeneratedSpeed();
		if (this.generatedSpeed != generatedSpeedNew) {
			this.generatedSpeed = generatedSpeedNew;
			network.onMemberGeneratedSpeedUpdated(this);
			if (network.tryRecalculateSpeed().isContradiction()) {
				popBlock();
			}
		}

		float stressImpactNew = entity.getStressImpact();
		if (this.stressImpact != stressImpactNew) {
			this.stressImpact = stressImpactNew;
			network.onMemberStressImpactUpdated();
		}

		float stressCapacityNew = entity.getStressCapacity();
		if (this.stressCapacity != stressCapacityNew) {
			this.stressCapacity = stressCapacityNew;
			network.onMemberStressCapacityUpdated();
		}
	}

	public boolean hasStressCapacity() {
		return stressCapacity != 0;
	}

	public boolean hasStressImpact() {
		return stressImpact != 0;
	}

	public float getTheoreticalSpeed(float speedAtRoot) {
		return speedAtRoot * speedRatio;
	}

	public float getStressCapacity() {
		return Math.abs(stressCapacity * generatedSpeed);
	}

	public float getTotalStressImpact(float speedAtRoot) {
		return Math.abs(stressImpact * getTheoreticalSpeed(speedAtRoot));
	}

	private SolveResult setNetwork(KineticNetwork network) {
		this.network.removeMember(this);
		this.network = network;
		network.addMember(this);
		return network.tryRecalculateSpeed();
	}

	public @Nullable KineticNode getSource() {
		return source;
	}

	private SolveResult setSource(KineticNode from, float ratio) {
		source = from;
		speedRatio = from.speedRatio * ratio;
		return setNetwork(from.network);
	}

	public void onAdded() {
		getActiveConnections()
				.findAny()
				.ifPresent(e -> {
					if (setSource(e.getFirst(), 1/e.getSecond()).isOk()) {
						propagateSource();
					} else {
						popBlock();
					}
				});
	}

	/**
	 * Propagates this node's source and network to any connected nodes that aren't yet part of the same network, then
	 * repeats this recursively with the connected nodes in a breadth-first order.
	 */
	private void propagateSource() {
		List<KineticNode> frontier = new LinkedList<>();
		frontier.add(this);

		while (!frontier.isEmpty()) {
			KineticNode cur = frontier.remove(0);
			for (Pair<KineticNode, Float> pair : cur.getActiveConnectionsList()) {
				KineticNode next = pair.getFirst();
				float ratio = pair.getSecond();

				if (next == cur.source) continue;

				if (next.network == network) {
					if (!Mth.equal(next.speedRatio, cur.speedRatio * ratio)) {
						// this node will cause a cycle with conflicting speed ratios
						if (network.isStopped()) {
							network.markConflictingCycle(cur, next);
						} else {
							popBlock();
							return;
						}
					}
					continue;
				}

				if (next.setSource(cur, ratio).isOk()) {
					frontier.add(next);
				} else {
					// this node will run against the network or activate a conflicting cycle
					popBlock();
					return;
				}
			}
		}
	}

	public void onRemoved() {
		network.removeMember(this);
		getActiveConnections()
				.map(Pair::getFirst)
				.filter(n -> n.source == this)
				.forEach(KineticNode::rerootHere);
	}

	private void rerootHere() {
		source = null;
		speedRatio = 1;
		SolveResult recalculateSpeedResult = setNetwork(new KineticNetwork(this));
		assert(recalculateSpeedResult.isOk());
		propagateSource();
	}

	/**
	 * Updates the speed of this node based on its network's root speed and its own speed ratio.
	 * @param speedAtRoot 	Current speed at the root of this node's network
	 * @return 				CONTRADICTION if the node's new speed exceeds the maximum value, and OK otherwise
	 */
	protected SolveResult tryUpdateSpeed(float speedAtRoot) {
		speedNext = getTheoreticalSpeed(speedAtRoot);
		if (Math.abs(speedNext) > AllConfigs.SERVER.kinetics.maxRotationSpeed.get())
			return SolveResult.CONTRADICTION;
		return SolveResult.OK;
	}

	protected void stop() {
		speedNext = 0;
	}

	public void flushChangedSpeed() {
		if (speedCur != speedNext) {
			speedCur = speedNext;
			entity.setSpeed(speedCur);
		}
	}

	public void popBlock() {
		// this should cause the node to get removed from the solver and lead to onRemoved() being called
		entity.getLevel().destroyBlock(entity.getBlockPos(), true);
	}

}
