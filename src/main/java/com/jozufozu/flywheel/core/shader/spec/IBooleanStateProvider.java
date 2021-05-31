package com.jozufozu.flywheel.core.shader.spec;

import com.jozufozu.flywheel.core.shader.gamestate.IGameStateProvider;

public interface IBooleanStateProvider extends IGameStateProvider {

	boolean isTrue();

	@Override
	default Boolean getValue() {
		return isTrue();
	}
}
