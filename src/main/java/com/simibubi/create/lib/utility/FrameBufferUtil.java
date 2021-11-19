package com.simibubi.create.lib.utility;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.simibubi.create.lib.extensions.RenderTargetExtensions;

import net.minecraft.client.Minecraft;

public class FrameBufferUtil {
	public static void enableStencil(RenderTarget buffer) {
		((RenderTargetExtensions) buffer).enableStencil();
	}

	public static boolean isStencilEnabled(RenderTarget buffer) {
		return ((RenderTargetExtensions) buffer).isStencilEnabled();
	}
}
