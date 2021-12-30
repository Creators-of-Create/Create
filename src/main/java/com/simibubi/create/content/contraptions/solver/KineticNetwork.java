package com.simibubi.create.content.contraptions.solver;

import com.simibubi.create.foundation.utility.Pair;

import javax.annotation.Nullable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class KineticNetwork {

	private final Set<KineticNode> members = new HashSet<>();
	private final Set<KineticNode> generators = new HashSet<>();
	private final Set<Pair<KineticNode, KineticNode>> conflictingCycles = new HashSet<>();
	private float rootSpeed;
	private @Nullable KineticNode mainGenerator;
	private boolean speedDirty;
	private boolean overstressed;

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

	public boolean isStopped() { return generators.isEmpty() || overstressed; }

	/**
	 * Recalculates the speed at the root node of this network.
	 * @return	CONTRADICTION if the network has cycles with conflicting speed ratios or generators turning against
	 * 			each other, and OK otherwise
	 */
	public SolveResult tryRecalculateSpeed() {
		if (!conflictingCycles.isEmpty() && !isStopped()) return SolveResult.CONTRADICTION;
		if (!speedDirty) return SolveResult.OK;

		SolveResult result = SolveResult.OK;

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
				result = SolveResult.CONTRADICTION;
				continue;
			}

			if (newSpeed < speedAtRoot * sign) {
				newSpeed = speedAtRoot * sign;
				newGenerator = generator;
			}
		}
		rootSpeed = newSpeed * sign;
		mainGenerator = newGenerator;
		speedDirty = false;

		if (overstressed) return SolveResult.OK;
		return result;
	}

	/**
	 * @return	a List of new networks created during this function call
	 */
	public List<KineticNetwork> tick() {
		List<KineticNetwork> newNetworks = updateMemberSpeeds();

		if (generators.isEmpty()) {
			overstressed = false;
			return newNetworks;
		}

		float stressImpact = (float) members.stream().mapToDouble(n -> n.getTotalStressImpact(rootSpeed)).sum();
		float stressCapacity = (float) members.stream().mapToDouble(KineticNode::getStressCapacity).sum();

		if (stressImpact > stressCapacity) {
			if (!overstressed) {
				overstressed = true;
				members.forEach(KineticNode::stop);
			}
		} else {
			if (overstressed) {
				overstressed = false;
				newNetworks.addAll(bulldozeContradictingMembers());
				newNetworks.addAll(updateMemberSpeeds());
			}
		}

		members.forEach(KineticNode::flushChangedSpeed);
		return newNetworks;
	}

	private List<KineticNetwork> updateMemberSpeeds() {
		// if we're stopped, then all members' speeds will be 0, so no need to check for speeding nodes
		if (isStopped()) {
			members.forEach(KineticNode::stop);
			return new LinkedList<>();
		}

		SolveResult recalculateSpeedResult = tryRecalculateSpeed();
		// generators should not be turning against each other or have conflicting cycles by now
		assert(recalculateSpeedResult.isOk());

		// update node speeds in a breadth-first order, checking for speeding nodes along the way
		List<KineticNetwork> newNetworks = new LinkedList<>();
		Set<KineticNode> visited = new HashSet<>();
		List<KineticNode> frontier = new LinkedList<>();
		frontier.add(mainGenerator);

		while (!frontier.isEmpty()) {
			KineticNode cur = frontier.remove(0);
			visited.add(cur);
			if (cur.tryUpdateSpeed(rootSpeed).isOk()) {
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

	private List<KineticNetwork> bulldozeContradictingMembers() {
		List<KineticNetwork> newNetworks = new LinkedList<>();

		// generators running against network
		float sign = Math.signum(rootSpeed);
		List<KineticNode> runningAgainst = generators.stream()
				.filter(n -> Math.signum(n.getGeneratedSpeedAtRoot()) != sign)
				.collect(Collectors.toList());
		runningAgainst.forEach(n -> { n.onPopBlock(); newNetworks.add(n.getNetwork()); });

		// conflicting cycles
		List<KineticNode> cycles = conflictingCycles.stream()
				.map(Pair::getFirst)
				.collect(Collectors.toList());
		cycles.forEach(n -> { n.onPopBlock(); newNetworks.add(n.getNetwork()); });

		return newNetworks;
	}

}
