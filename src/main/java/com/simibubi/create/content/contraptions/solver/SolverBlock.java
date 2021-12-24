package com.simibubi.create.content.contraptions.solver;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface SolverBlock {
	void created(KineticSolver solver, Level level, BlockPos pos);
}
