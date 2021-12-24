package com.simibubi.create.content.contraptions.solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.simibubi.create.foundation.utility.WorldAttached;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class KineticSolver {

	private static final WorldAttached<KineticSolver> SOLVERS = new WorldAttached<>($ -> new KineticSolver());

	public static KineticSolver getSolver(Level level) {
		return SOLVERS.get(level);
	}

	private final PropertyMap properties = new PropertyMap();
	private final Map<BlockPos, Set<RewriteRule.Tracker<?>>> rules = new HashMap<>();
	private final HashSet<RewriteRule.Tracker<?>> allRules = new HashSet<>();

	private Set<RewriteRule.Tracker<?>> rulesFrontier = new HashSet<>();

	public <T> RewriteRule<T> addRule(BlockPos pos, RewriteRule.Descriptor<T> ruleDesc) {
		RewriteRule<T> rule = new RewriteRule<>(ruleDesc);
		RewriteRule.Tracker<?> tracker = new RewriteRule.Tracker<>(rule, pos, properties::trackReader);
		rules.computeIfAbsent(pos, $ -> new HashSet<>()).add(tracker);
		allRules.add(tracker);
		rulesFrontier.add(tracker);
		return rule;
	}

	public void removeRule(BlockPos pos, RewriteRule<?> rule) {
		Set<RewriteRule.Tracker<?>> trackers = rules.get(pos);
		if (trackers == null) return;
		trackers.stream()
				.filter(t -> t.rule == rule)
				.findAny()
				.ifPresent(tracker -> {
					allRules.remove(tracker);
					trackers.remove(tracker);
					if (trackers.isEmpty()) {
						rules.remove(pos);
					}
					properties.untrackReader(tracker);
					rulesFrontier.addAll(properties.unwrite(tracker.writes));
				});
	}

	public void removeAllRules(BlockPos pos) {
		Set<RewriteRule.Tracker<?>> trackers = rules.remove(pos);
		if (trackers == null) return;
		for (RewriteRule.Tracker<?> tracker: trackers) {
			allRules.remove(tracker);
			properties.untrackReader(tracker);
		}
		for (RewriteRule.Tracker<?> tracker: trackers) {
			rulesFrontier.addAll(properties.unwrite(tracker.writes));
		}
	}

	public Set<BlockPos> solve() {
		Set<BlockPos> contradictions = new HashSet<>();

		while (!rulesFrontier.isEmpty()) {
			Set<RewriteRule.Tracker<?>> next = new HashSet<>();

			for (RewriteRule.Tracker<?> rule : rulesFrontier) {
				if (!allRules.contains(rule) || !rule.canRewrite()) continue;

				PropertyMap.WriteResult res = rule.rewrite(properties);
				if (res instanceof PropertyMap.WriteResult.Ok ok) {
					next.addAll(ok.readyToRewrite);
				} else if (res instanceof PropertyMap.WriteResult.Contradiction) {
					removeAllRules(rule.pos);
					contradictions.add(rule.pos);
				}
			}

			rulesFrontier = next;
		}

		return contradictions;
	}
}
