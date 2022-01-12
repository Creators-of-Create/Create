package com.simibubi.create.foundation.render;

import javax.annotation.Nonnull;

import com.jozufozu.flywheel.core.compile.ShaderConstants;
import com.jozufozu.flywheel.core.shader.GameStateProvider;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.KineticDebugger;

import net.minecraft.resources.ResourceLocation;

public enum RainbowDebugStateProvider implements GameStateProvider {
	INSTANCE;
	public static final ResourceLocation NAME = Create.asResource("rainbow_debug");

	@Override
	public boolean isTrue() {
		return KineticDebugger.isActive();
	}

	@Nonnull
	@Override
	public ResourceLocation getID() {
		return NAME;
	}

	@Override
	public void alterConstants(@Nonnull ShaderConstants constants) {
		constants.define("DEBUG_RAINBOW");
	}
}
