package com.simibubi.create.content.contraptions.solver;

import net.minecraft.core.BlockPos;

public abstract class ConnectionGoal implements Goal {
	public final Connection connection;

	public ConnectionGoal(Connection connection) {
		this.connection = connection;
	}

	@Override
	public void onAdded(KineticSolver.PropertyMap solver) {
		solver.addConnection(connection);
	}

	public static class EqualSpeed extends ConnectionGoal {
		public EqualSpeed(Connection connection) {
			super(connection);
		}

		@Override
		public BlockPos getPos() {
			return connection.from;
		}

		@Override
		public SolveResult solve(KineticSolver.PropertyMap solver) {
			if (solver.isComplete(connection)) {
				Value toSpeed = solver.getOrCreateProperty(connection.to, "speed");
				Value fromSpeed = solver.getOrCreateProperty(connection.from, "speed");
				if (toSpeed instanceof Value.Known toValue && fromSpeed instanceof Value.Known fromValue) {
					if (toValue.value != fromValue.value) {
						return Goal.SolveResult.CONTRADICTION;
					}
				} else {
					solver.setProperty(connection.to, "speed", fromSpeed);
				}
			}
			return Goal.SolveResult.OK;
		}
	}
}
