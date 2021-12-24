package com.simibubi.create.content.contraptions.solver;

import java.util.Optional;

public class ConstantSpeedRule implements RewriteRule.Descriptor<Float> {
	private final float speed;

	public ConstantSpeedRule(float speed) {
		this.speed = speed;
	}

	@Override
	public Property.Type<Float> getWrittenProperty() {
		return AllPropertyTypes.SPEED;
	}

	@Override
	public Optional<Float> getRewrittenValue(RewriteRule.PropertyReader reader) {
		return Optional.of(speed);
	}
}
