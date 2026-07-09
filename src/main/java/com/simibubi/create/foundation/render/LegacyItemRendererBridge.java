package com.simibubi.create.foundation.render;

import com.simibubi.create.foundation.render.ItemRenderer;

/**
 * Temporary Create 26.2 porting bridge for call sites still using the old item renderer.
 * TODO 26.2: Replace with ItemModelResolver + ItemStackRenderState submit calls.
 */
@Deprecated(forRemoval = true)
public class LegacyItemRendererBridge {
	private static final ItemRenderer RENDERER = new ItemRenderer();

	public static ItemRenderer getItemRenderer() {
		return RENDERER;
	}
}
