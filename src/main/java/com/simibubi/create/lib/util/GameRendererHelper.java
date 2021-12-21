package com.simibubi.create.lib.util;

import com.simibubi.create.lib.mixin.accessor.GameRendererAccessor;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;

public final class GameRendererHelper {
	public static double getFOVModifier(GameRenderer renderer, Camera camera, float tickDelta, boolean changingFov) {
		return get(renderer).create$getFOVModifier(camera, tickDelta, changingFov);
	}

	private static GameRendererAccessor get(GameRenderer renderer) {
		return MixinHelper.cast(renderer);
	}

	private GameRendererHelper() {}
}
