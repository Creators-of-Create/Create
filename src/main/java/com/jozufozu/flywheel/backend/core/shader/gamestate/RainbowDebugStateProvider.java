package com.jozufozu.flywheel.backend.core.shader.gamestate;

import com.jozufozu.flywheel.backend.core.shader.spec.IBooleanStateProvider;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.KineticDebugger;

import net.minecraft.util.ResourceLocation;

public class RainbowDebugStateProvider implements IBooleanStateProvider {

	public static final RainbowDebugStateProvider INSTANCE = new RainbowDebugStateProvider();
	public static final ResourceLocation NAME = new ResourceLocation(Create.ID, "rainbow_debug");

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
