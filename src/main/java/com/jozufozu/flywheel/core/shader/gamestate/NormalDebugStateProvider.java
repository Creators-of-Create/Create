package com.jozufozu.flywheel.core.shader.gamestate;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.core.shader.spec.IBooleanStateProvider;

import net.minecraft.util.ResourceLocation;

public class NormalDebugStateProvider implements IBooleanStateProvider {

	public static final NormalDebugStateProvider INSTANCE = new NormalDebugStateProvider();
	public static final ResourceLocation NAME = new ResourceLocation(Flywheel.ID, "normal_debug");

	protected NormalDebugStateProvider() {

	}

	@Override
	public boolean isTrue() {
		return false;
	}

	@Override
	public ResourceLocation getID() {
		return NAME;
	}
}
