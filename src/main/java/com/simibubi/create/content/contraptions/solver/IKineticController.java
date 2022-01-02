package com.simibubi.create.content.contraptions.solver;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public interface IKineticController {
	default void onUpdate(Level level, KineticSolver solver, KineticNode node) { }

	default KineticConnections getConnections() {
		return AllConnections.EMPTY;
	}

	default float getGeneratedSpeed() {
		return 0;
	}

	default float getStressImpact() {
		return 0;
	}

	default float getStressCapacity() {
		return 0;
	}

	default boolean isStressConstant() {
		return false;
	}

	default CompoundTag save(CompoundTag tag) { return tag; }
}
