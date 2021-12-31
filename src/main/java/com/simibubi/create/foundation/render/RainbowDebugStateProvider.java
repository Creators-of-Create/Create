package com.simibubi.create.foundation.render;

import com.jozufozu.flywheel.core.shader.gamestate.IGameStateProvider;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.KineticDebugger;

import net.minecraft.resources.ResourceLocation;

public class RainbowDebugStateProvider implements IGameStateProvider {

	public static final RainbowDebugStateProvider INSTANCE = new RainbowDebugStateProvider();
	public static final ResourceLocation NAME = Create.asResource("rainbow_debug");

	protected RainbowDebugStateProvider() {

	}

	@Override
	public boolean isTrue() {
		return KineticDebugger.isActive();
	}

	@Override
	public ResourceLocation getID() {
		return NAME;
	}
}
