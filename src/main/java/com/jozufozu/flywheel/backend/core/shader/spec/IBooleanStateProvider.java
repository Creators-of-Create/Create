package com.jozufozu.flywheel.backend.core.shader.spec;

import com.jozufozu.flywheel.backend.core.shader.gamestate.IGameStateProvider;

public interface IBooleanStateProvider extends IGameStateProvider {

	boolean isTrue();

	@Override
	default Boolean getValue() {
		return isTrue();
	}
}
