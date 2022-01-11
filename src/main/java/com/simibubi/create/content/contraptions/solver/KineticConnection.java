package com.simibubi.create.content.contraptions.solver;

import net.minecraft.core.Vec3i;

import java.util.Map;

public record KineticConnection(String name) {
	public Map<Vec3i, Map<KineticConnection, Float>> getRatios() {
		return KineticConnectionsRegistry.getConnections(this);
	}
}
