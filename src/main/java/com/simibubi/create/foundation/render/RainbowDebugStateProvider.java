package com.simibubi.create.foundation.render;

import javax.annotation.Nonnull;

import com.jozufozu.flywheel.core.shader.GameStateProvider;
import com.jozufozu.flywheel.core.shader.ShaderConstants;
import com.simibubi.create.content.kinetics.KineticDebugger;

public enum RainbowDebugStateProvider implements GameStateProvider {
	INSTANCE;

	@Override
	public boolean isTrue() {
		return KineticDebugger.isActive();
	}

	@Override
	public void alterConstants(@Nonnull ShaderConstants constants) {
		constants.define("DEBUG_RAINBOW");
	}
}
