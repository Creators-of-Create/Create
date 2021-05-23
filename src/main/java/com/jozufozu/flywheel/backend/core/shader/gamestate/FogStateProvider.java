package com.jozufozu.flywheel.backend.core.shader.gamestate;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.core.shader.GlFog;

import net.minecraft.util.ResourceLocation;

public class FogStateProvider implements IGameStateProvider {

	public static final FogStateProvider INSTANCE = new FogStateProvider();
	public static final ResourceLocation NAME = new ResourceLocation(Flywheel.ID, "fog_mode");

	@Override
	public ResourceLocation getID() {
		return NAME;
	}

	@Override
	public Object getValue() {
		return GlFog.getFogMode();
	}
}
