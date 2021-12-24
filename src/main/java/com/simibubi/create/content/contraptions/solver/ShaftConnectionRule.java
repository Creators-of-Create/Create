package com.simibubi.create.content.contraptions.solver;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

import java.util.Optional;
import java.util.Set;

public class ShaftConnectionRule implements RewriteRule.Descriptor<Set<Vec3i>> {
	private final Set<Vec3i> connections;

	public ShaftConnectionRule(Direction.Axis axis) {
		Direction positive = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
		connections = Set.of(positive.getNormal(), positive.getOpposite().getNormal());
	}

	@Override
	public Property.Type<Set<Vec3i>> getWrittenProperty() {
		return AllPropertyTypes.SHAFT_CONNECTIONS;
	}

	@Override
	public Optional<Set<Vec3i>> getRewrittenValue(RewriteRule.PropertyReader reader) {
		return Optional.of(connections);
	}
}
