package com.simibubi.create.lib.util;

import com.simibubi.create.lib.mixin.accessor.LivingEntityRendererAccessor;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

public class LivingRendererHelper {
	public static boolean addRenderer(LivingEntityRenderer renderer, RenderLayer toAdd) {
		return ((LivingEntityRendererAccessor) renderer).create$addLayer(toAdd);
	}
}
