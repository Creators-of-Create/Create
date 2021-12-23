package com.simibubi.create.content.contraptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public class KineticSolver {

	private final Map<BlockPos, Connection> connectionsFrom = new HashMap<>();
	private final Map<BlockPos, Connection> connectionsTo = new HashMap<>();

	private final List<Goal> goals = new ArrayList<>();

	private boolean needsUpdate;

	public void solve() {
		if (!needsUpdate) return;
		needsUpdate = false;


	}

	public void addFact(BlockPos pos) {

	}

	public void addGoal(Goal goal) {
		goals.add(goal);
		needsUpdate = true;
	}

	public interface SolverBlock {
		void created(KineticSolver solver, Level level, BlockPos pos);
	}

	public static abstract class Connection {
		public final BlockPos from;
		public final BlockPos to;

		public Connection(BlockPos from, BlockPos to) {
			this.from = from;
			this.to = to;
		}

		public abstract boolean isCompatible(Connection that);

		public static final class Shaft extends Connection {

			public Shaft(BlockPos pos, Direction face) {
				super(pos, pos.relative(face));
			}

			@Override
			public boolean isCompatible(Connection that) {
				return that instanceof Shaft;
			}
		}
	}

	public static class Goal {
		public final Connection connection;

		public Goal(Connection connection) {
			this.connection = connection;
		}

		public static final class EqualSpeed extends Goal {
			public EqualSpeed(Connection connection) {
				super(connection);
			}
		}
	}
}
