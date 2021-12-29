package com.simibubi.create.content.contraptions.solver;

import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.util.Mth;

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

	public KineticNetwork(KineticNode root) {
		addMember(root);
		rootSpeed = root.getGeneratedSpeed();
	}

	public void addMember(KineticNode node) {
		members.add(node);
		if (node.isGenerator()) {
			generators.add(node);
		}
	}

	public void updateMember(KineticNode node) {
		if (!members.contains(node)) throw new IllegalArgumentException();
		if (node.isGenerator()) {
			generators.add(node);
		} else {
			generators.remove(node);
		}
	}

	public void removeMember(KineticNode node) {
		members.remove(node);
		if (node.isGenerator()) {
			generators.remove(node);
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

	/**
	 * Recalculates the speed at the root of this network, and if it has changed, recalculates the speed of all
	 * KineticNodes in the network and pops any nodes whose speed has increased above the speed limit.
	 * @param checkRoot Node to start performing a breadth-first from in order to find and pop speeding nodes. If null,
	 *                  speeding nodes are ignored.
	 * @param forced	If true, will check for speeding nodes from checkRoot even if the root speed has not changed.
	 * @return			CONTRADICTION if the network currently has a cycle with conflicting speed ratios or
	 * 					has generators turning against each other, and OK otherwise.
	 */
	public SolveResult recalculateSpeed(@Nullable KineticNode checkRoot, boolean forced) {
		if (!conflictingCycles.isEmpty() && !generators.isEmpty()) {
			// cycle with conflicting speed ratios is present
			return SolveResult.CONTRADICTION;
		}

		// find the generator that would maximize the root speed
		float newSpeed = 0;
		float sign = 0;
		for (KineticNode generator : generators) {
			float speedAtRoot = generator.getGeneratedSpeedAtRoot();
			if (newSpeed == 0) {
				sign = Math.signum(speedAtRoot);
			} else if (Math.signum(speedAtRoot) != sign) {
				// generators are turning against each other
				return SolveResult.CONTRADICTION;
			}
			newSpeed = Math.max(newSpeed, sign * speedAtRoot);
		}
		newSpeed *= sign;

		if (!Mth.equal(rootSpeed, newSpeed)) {
			rootSpeed = newSpeed;

			if (checkRoot == null) {
				members.forEach(KineticNode::tryUpdateSpeed);
			} else {
				updateNodeSpeeds(checkRoot, false);
			}
		} else if (forced) {
			updateNodeSpeeds(checkRoot, true);
		}

		return SolveResult.OK;
	}

	private void updateNodeSpeeds(KineticNode root, boolean followSources) {
		Set<KineticNode> visited = new HashSet<>();
		List<KineticNode> frontier = new LinkedList<>();
		frontier.add(root);

		// update node speeds in a breadth-first order, starting at root
		while (!frontier.isEmpty()) {
			KineticNode cur = frontier.remove(0);
			visited.add(cur);
			if (cur.tryUpdateSpeed().isOk()) {
				for (KineticNode next : cur.getActiveConnections().keySet()) {
					if (!(visited.contains(next) || (followSources && !cur.isSourceOf(next))))
						frontier.add(next);
				}
			} else {
				// stop searching on this branch once a speeding node is found
				cur.onPopBlock();
			}
		}
	}

}
