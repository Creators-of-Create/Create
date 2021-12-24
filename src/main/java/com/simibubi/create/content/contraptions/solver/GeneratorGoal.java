package com.simibubi.create.content.contraptions.solver;

import net.minecraft.core.BlockPos;

public final class GeneratorGoal implements Goal {
	public final BlockPos me;
	public final float speed;

	public GeneratorGoal(BlockPos me, float speed) {
		this.me = me;
		this.speed = speed;
	}

	@Override
	public BlockPos getPos() {
		return me;
	}

	@Override
	public SolveResult solve(KineticSolver.PropertyMap solver) {
		solver.setProperty(me, "speed", new Value.Known(speed));
		return Goal.SolveResult.OK;
	}
}
