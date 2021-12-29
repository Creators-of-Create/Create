package com.simibubi.create.content.contraptions.solver;

import com.simibubi.create.foundation.utility.Pair;

import javax.annotation.Nullable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class KineticNetwork {

	private final Set<KineticNode> members = new HashSet<>();
	private final Set<KineticNode> generators = new HashSet<>();
	private final Set<Pair<KineticNode, KineticNode>> conflictingCycles = new HashSet<>();
	private float rootSpeed;
	private @Nullable KineticNode mainGenerator;
	private boolean speedDirty;

	public KineticNetwork(KineticNode root) {
		addMember(root);
	}

	public void addMember(KineticNode node) {
		members.add(node);
		if (node.isGenerator() && !generators.contains(node)) {
			generators.add(node);
			speedDirty = true;
		}
	}

	public void updateMember(KineticNode node) {
		if (!members.contains(node)) throw new IllegalArgumentException();

		if (node.isGenerator()) {
			generators.add(node);
		} else {
			generators.remove(node);
		}
		speedDirty = true;
	}

	public void removeMember(KineticNode node) {
		members.remove(node);
		if (node.isGenerator() && generators.contains(node)) {
			generators.remove(node);
			speedDirty = true;
		}
		conflictingCycles.removeIf(p -> p.getFirst() == node || p.getSecond() == node);
	}

	public void markConflictingCycle(KineticNode from, KineticNode to) {
		if (!members.contains(from) || !members.contains(to)) throw new IllegalArgumentException();
		conflictingCycles.add(Pair.of(from, to));
	}

	public float getRootSpeed() {
		return rootSpeed;
	}

	public boolean isStopped() { return generators.isEmpty(); }

	/**
	 * Recalculates the speed at the root node of this network.
	 * @return	CONTRADICTION if the network has generators turning against each other, and OK otherwise
	 */
	public SolveResult recalculateSpeed() {
		if (!speedDirty) return SolveResult.OK;

		float newSpeed = 0;
		KineticNode newGenerator = null;
		float sign = 0;

		// search over all generators to maximize the root speed
		for (KineticNode generator : generators) {
			float speedAtRoot = generator.getGeneratedSpeedAtRoot();

			if (newSpeed == 0) {
				sign = Math.signum(speedAtRoot);
			}

			if (Math.signum(speedAtRoot) != sign) {
				// generators are turning against each other
				return SolveResult.CONTRADICTION;
			}

			if (newSpeed < speedAtRoot * sign) {
				newSpeed = speedAtRoot * sign;
				newGenerator = generator;
			}
		}
		rootSpeed = newSpeed * sign;
		mainGenerator = newGenerator;
		speedDirty = false;
		return SolveResult.OK;
	}

	/**
	 * @return	a List of new networks created during this function call
	 */
	public List<KineticNetwork> tick() {
		List<KineticNetwork> newNetworks = updateMemberSpeeds();
		members.forEach(KineticNode::flushChangedSpeed);
		return newNetworks;
	}

	private List<KineticNetwork> updateMemberSpeeds() {
		SolveResult recalculateSpeedResult = recalculateSpeed();
		// generators should not be turning against each other by now
		assert(recalculateSpeedResult.isOk());

		// if we're stopped then all members' speeds will be 0, so no need to check for speeding nodes
		if (isStopped()) {
			members.forEach(KineticNode::tryUpdateSpeed);
			return List.of();
		}

		// there should be no cycles with conflicting speed ratios by now
		assert(conflictingCycles.isEmpty());

		// update node speeds in a breadth-first order, checking for speeding nodes along the way
		List<KineticNetwork> newNetworks = new LinkedList<>();
		Set<KineticNode> visited = new HashSet<>();
		List<KineticNode> frontier = new LinkedList<>();
		frontier.add(mainGenerator);

		while (!frontier.isEmpty()) {
			KineticNode cur = frontier.remove(0);
			visited.add(cur);
			if (cur.tryUpdateSpeed().isOk()) {
				cur.getActiveConnections()
						.map(Pair::getFirst)
						.filter(n -> !visited.contains(n))
						.forEach(frontier::add);
			} else {
				// stop searching on this branch once a speeding node is found
				cur.onPopBlock();
				newNetworks.add(cur.getNetwork());
			}
		}

		return newNetworks;
	}

}
