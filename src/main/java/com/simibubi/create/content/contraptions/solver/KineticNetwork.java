package com.simibubi.create.content.contraptions.solver;

import com.simibubi.create.foundation.utility.Pair;

import java.util.HashSet;
import java.util.Set;

public class KineticNetwork {

	private final Set<KineticNode> members = new HashSet<>();
	private final Set<KineticNode> generators = new HashSet<>();
	private final Set<Pair<KineticNode, KineticNode>> conflictingCycles = new HashSet<>();
	private float rootSpeed;
	private boolean speedDirty;

	public KineticNetwork(KineticNode root) {
		addMember(root);
		rootSpeed = root.getGeneratedSpeed();
		speedDirty = false;
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

	public SolveResult recalculateSpeed() {
		if (!conflictingCycles.isEmpty() && !generators.isEmpty()) return SolveResult.CONTRADICTION;
		if (!speedDirty) return SolveResult.OK;

		float newSpeed = 0;
		float sign = 0;
		for (KineticNode generator : generators) {
			float speedAtRoot = generator.getGeneratedSpeedAtRoot();
			if (newSpeed == 0) {
				sign = Math.signum(speedAtRoot);
				newSpeed = sign * speedAtRoot;
			} else {
				if (Math.signum(speedAtRoot) != sign)
					return SolveResult.CONTRADICTION;
				newSpeed = Math.max(newSpeed, sign * speedAtRoot);
			}
		}
		newSpeed *= sign;

		if (rootSpeed != newSpeed) {
			rootSpeed = newSpeed;
			for (KineticNode member : members) {
				member.onSpeedUpdated();
			}
		}

		speedDirty = false;
		return SolveResult.OK;
	}

}
