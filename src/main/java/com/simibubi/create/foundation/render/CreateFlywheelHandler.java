package com.simibubi.create.foundation.render;

import com.jozufozu.flywheel.backend.Backend;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.render.effects.EffectsContext;

public class CreateFlywheelHandler {
	public static void init() {
		Backend.register(ContraptionRenderDispatcher.TILES);
		Backend.register(ContraptionRenderDispatcher.STRUCTURE);
		Backend.register(EffectsContext.INSTANCE);
	}
}
