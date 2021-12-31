package com.simibubi.create.content.contraptions.solver;

import com.simibubi.create.foundation.utility.Pair;

import com.simibubi.create.foundation.utility.ResetableLazy;

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

	private float rootTheoreticalSpeed;
	private boolean rootSpeedDirty;
	private @Nullable KineticNode mainGenerator;

	private boolean overstressed;

	private boolean rootSpeedChanged;
	private final Set<KineticNode> potentialNewBranches = new HashSet<>();

	private final ResetableLazy<Float> totalStressImpact = ResetableLazy.of(() ->
			(float) members.stream().mapToDouble(n -> n.getTotalStressImpact(rootTheoreticalSpeed)).sum());
	private final ResetableLazy<Float> totalStressCapacity = ResetableLazy.of(() ->
			(float) members.stream().mapToDouble(KineticNode::getStressCapacity).sum());

	private boolean ticked;
	private final Set<KineticNode> stressConnectors = new HashSet<>();

	public KineticNetwork(KineticNode root) {
		addMember(root);
	}

	public void addMember(KineticNode node) {
		members.add(node);
		potentialNewBranches.add(node);
		if (node.getConnections().hasStressOnlyConnections()) stressConnectors.add(node);

		if (node.isGenerator() && !generators.contains(node)) {
			generators.add(node);
			rootSpeedDirty = true;
			rootSpeedChanged = true;
		}
		if (node.hasStressImpact()) onMemberStressImpactUpdated();
		if (node.hasStressCapacity()) onMemberStressCapacityUpdated();
	}

	public void onMemberGeneratedSpeedUpdated(KineticNode node) {
		if (node.isGenerator()) {
			generators.add(node);
		} else {
			generators.remove(node);
		}
		rootSpeedDirty = true;
		rootSpeedChanged = true;
	}

	public void onMemberStressImpactUpdated() {
		totalStressImpact.reset();
	}

	public void onMemberStressCapacityUpdated() {
		totalStressCapacity.reset();
	}

	public void removeMember(KineticNode node) {
		if (node.isGenerator() && generators.contains(node)) {
			generators.remove(node);
			rootSpeedDirty = true;
			rootSpeedChanged = true;
		}
		if (node.hasStressImpact()) onMemberStressImpactUpdated();
		if (node.hasStressCapacity()) onMemberStressCapacityUpdated();

		members.remove(node);
		stressConnectors.remove(node);
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
		SolveResult result = tryRecalculateTheoreticalSpeed();
		if (isStopped()) return SolveResult.OK;
		return result;
	}

	private SolveResult tryRecalculateTheoreticalSpeed() {
		SolveResult result = conflictingCycles.isEmpty() ? SolveResult.OK : SolveResult.CONTRADICTION;
		if (!rootSpeedDirty) return result;

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

		rootTheoreticalSpeed = newSpeed * sign;
		mainGenerator = newGenerator;
		rootSpeedDirty = false;

		return result;
	}

	public float getTotalStressImpact() {
		return totalStressImpact.get();
	}

	public float getTotalStressCapacity() {
		return totalStressCapacity.get();
	}

	private float getRootSpeed() {
		return isStopped() ? 0 : rootTheoreticalSpeed;
	}

	public void untick() {
		ticked = false;
	}

	public void tick(List<KineticNetwork> newNetworks) {
		if (ticked) return;

		Set<KineticNetwork> stressConnected = stressConnectors.stream()
				.flatMap(KineticNode::getActiveStressOnlyConnections)
				.collect(Collectors.toSet());
		stressConnected.add(this);

		float stressImpact = 0;
		float stressCapacity = 0;

		for (KineticNetwork cur : stressConnected) {
			cur.ticked = true;
			cur.updateMemberSpeeds(newNetworks);
			stressImpact += cur.getTotalStressImpact();
			stressCapacity += cur.getTotalStressCapacity();
		}

		boolean nowOverstressed = stressImpact > stressCapacity;

		for (KineticNetwork cur : stressConnected) {
			if (cur.generators.isEmpty()) {
				cur.overstressed = false;
			} else if (nowOverstressed) {
				if (!cur.overstressed) {
					cur.overstressed = true;
					rootSpeedChanged = true;
					cur.members.forEach(KineticNode::stop);
				}
			} else {
				if (cur.overstressed) {
					cur.overstressed = false;
					rootSpeedChanged = true;
					cur.bulldozeContradictingMembers(newNetworks);
					cur.updateMemberSpeeds(newNetworks);
				}
			}

			cur.members.forEach(KineticNode::flushChangedSpeed);
		}
	}

	/**
	 * Update the speed of every member, starting from the main generator and popping off speeding nodes along the way
	 * @param newNetworks 	a List that any new networks created during this call will be added to
	 */
	private void updateMemberSpeeds(List<KineticNetwork> newNetworks) {
		// if we're stopped, then all members' speeds will be 0, so no need to check for speeding nodes
		if (isStopped()) {
			members.forEach(KineticNode::stop);
			return;
		}

		SolveResult recalculateSpeedResult = tryRecalculateSpeed();
		// generators should not be turning against each other or have conflicting cycles by now
		assert(recalculateSpeedResult.isOk());

		if (rootSpeedChanged) {
			// root speed changed, update all nodes starting from the main generator
			rootSpeedChanged = false;
			bfs(mainGenerator, newNetworks, false);
		} else if (!potentialNewBranches.isEmpty()) {
			// new nodes added, update only the new network branches
			potentialNewBranches.stream()
					.filter(n -> !potentialNewBranches.contains(n.getSource()))
					.forEach(n -> bfs(n, newNetworks, true));
			potentialNewBranches.clear();
		}
	}

	private void bfs(KineticNode root, List<KineticNetwork> newNetworks, boolean followSource) {
		// update node speeds in a breadth-first order, checking for speeding nodes along the way
		Set<KineticNode> visited = new HashSet<>();
		List<KineticNode> frontier = new LinkedList<>();
		frontier.add(root);

		while (!frontier.isEmpty()) {
			KineticNode cur = frontier.remove(0);
			if (!members.contains(cur) || visited.contains(cur)) continue;
			visited.add(cur);

			if (cur.tryUpdateSpeed(getRootSpeed()).isOk()) {
				cur.getActiveConnections()
						.map(Pair::getFirst)
						.filter(n -> !followSource || n.getSource() == cur)
						.forEach(frontier::add);
			} else {
				// stop searching on this branch once a speeding node is found
				cur.popBlock();
				newNetworks.add(cur.getNetwork());
			}
		}
	}

	private void bulldozeContradictingMembers(List<KineticNetwork> newNetworks) {
		/*
		This method is necessary to handle the edge case where contradicting nodes have been added to the network while
		it was overstressed and now that it's moving again we need to pop them. Here we can't just stop following a
		branch after popping a block though since there may be more contradictions further down that branch, so we'll
		just pop all potentially contradicting nodes off and hope no one cares
		 */

		// generators running against network
		float sign = Math.signum(rootTheoreticalSpeed);
		List<KineticNode> runningAgainst = generators.stream()
				.filter(n -> Math.signum(n.getGeneratedSpeedAtRoot()) != sign)
				.collect(Collectors.toList());
		runningAgainst.forEach(n -> { n.popBlock(); newNetworks.add(n.getNetwork()); });

		// conflicting cycles
		List<KineticNode> cycles = conflictingCycles.stream()
				.map(Pair::getFirst)
				.collect(Collectors.toList());
		cycles.forEach(n -> { n.popBlock(); newNetworks.add(n.getNetwork()); });
	}

}
