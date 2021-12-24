package com.simibubi.create.content.contraptions.solver;

import net.minecraft.core.BlockPos;

public interface Goal {
	BlockPos getPos();

	SolveResult solve(KineticSolver.PropertyMap solver);

	default void onAdded(KineticSolver.PropertyMap solver) {
	}

	enum SolveResult {
		CONTRADICTION,
		OK,
	}
}
