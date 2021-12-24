package com.simibubi.create.content.contraptions.solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

	private final List<Goal> goals = new ArrayList<>();

	public static class PropertyMap {
		private final Map<BlockPos, Map<String, Value>> properties = new HashMap<>();
		private final Map<BlockPos, Set<Connection>> connections = new HashMap<>();

		public Optional<Value> getProperty(BlockPos pos, String property) {
			Map<String, Value> map = properties.get(pos);

			if (map != null) {
				return Optional.of(map.computeIfAbsent(property, $ -> new Value.Unknown()));
			} else {
				return Optional.empty();
			}
		}

		public Value getOrCreateProperty(BlockPos pos, String property) {
			return properties.computeIfAbsent(pos, $ -> new HashMap<>())
					.computeIfAbsent(property, $ -> new Value.Unknown());
		}

		public void setProperty(BlockPos pos, String property, Value value) {
			properties.computeIfAbsent(pos, $ -> new HashMap<>())
					.put(property, value);
		}

		public boolean isComplete(Connection connection) {
			Set<Connection> connections = this.connections.get(connection.to);

			if (connections == null) return false;

			for (Connection other : connections) {
				if (connection.isCompatible(other) && other.to.equals(connection.from)) {
					return true;
				}
			}

			return false;
		}

		public void addConnection(Connection connection) {
			properties.computeIfAbsent(connection.from, $ -> new HashMap<>());
			connections.computeIfAbsent(connection.from, $ -> new HashSet<>()).add(connection);
		}

		public void clear() {
			properties.clear();
		}
	}

	private boolean needsUpdate;

	public List<BlockPos> solve() {
		if (!needsUpdate) return Collections.emptyList();
		needsUpdate = false;

		List<BlockPos> troublemakers = new ArrayList<>();

		outer:
		while (true) {
			properties.clear();

			for (Goal goal : goals) {
				Goal.SolveResult result = goal.solve(properties);

				switch (result) {
				case CONTRADICTION -> {
					troublemakers.add(goal.getPos());
					continue outer;
				}
				case OK -> {}
				}
			}
			break;
		}

		return troublemakers;
	}

	public void addGoal(Goal goal) {
		goals.add(goal);
		needsUpdate = true;
		goal.onAdded(properties);
	}
}
