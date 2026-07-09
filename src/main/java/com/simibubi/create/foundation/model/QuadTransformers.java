package com.simibubi.create.foundation.model;

import java.util.List;

public final class QuadTransformers {
	private QuadTransformers() {}

	public static Transformer settingMaxEmissivity() {
		return Transformer.INSTANCE;
	}

	public enum Transformer {
		INSTANCE;

		public void processInPlace(List<?> quads) {}
	}
}
