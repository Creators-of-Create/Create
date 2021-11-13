package com.simibubi.create.lib.utility;

import com.mojang.blaze3d.pipeline.RenderTarget;

import net.minecraft.client.Minecraft;

public class FrameBufferUtil {
	public static void enableStencil(RenderTarget buffer) {
		buffer.resize(buffer.viewWidth, buffer.viewHeight, Minecraft.ON_OSX);
	}
}
