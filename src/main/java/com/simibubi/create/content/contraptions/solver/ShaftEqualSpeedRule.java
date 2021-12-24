package com.simibubi.create.content.contraptions.solver;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

import java.util.Optional;
import java.util.Set;

public class ShaftEqualSpeedRule implements RewriteRule.Descriptor<Float> {
	private final Vec3i to, from;
	private final Property.Relative<Set<Vec3i>> otherConnections;
	private final Property.Relative<Float> otherSpeed;

	public ShaftEqualSpeedRule(Direction dir) {
		to = dir.getNormal();
		from = dir.getOpposite().getNormal();
		otherConnections = new Property.Relative<>(to, AllPropertyTypes.SHAFT_CONNECTIONS);
		otherSpeed = new Property.Relative<>(to, AllPropertyTypes.SPEED);
	}

	@Override
	public Property.Type<Float> getWrittenProperty() {
		return AllPropertyTypes.SPEED;
	}

	@Override
	public Set<Property.Relative<?>> getReadProperties() {
		return Set.of(otherConnections, otherSpeed);
	}

	@Override
	public Optional<Float> getRewrittenValue(RewriteRule.PropertyReader reader) {
		Set<Vec3i> otherConnections = reader.read(this.otherConnections);
		float otherSpeed = reader.read(this.otherSpeed);

		if (otherConnections.contains(from))
			return Optional.of(otherSpeed);
		return Optional.empty();
	}
}
