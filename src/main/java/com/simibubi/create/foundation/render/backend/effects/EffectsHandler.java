package com.simibubi.create.foundation.render.backend.effects;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;

public class EffectsHandler {

	final Minecraft mc;

	private final Framebuffer framebuffer;

	public EffectsHandler(Minecraft minecraft) {
		this.mc = minecraft;

		Framebuffer render = minecraft.getFramebuffer();
		framebuffer = new Framebuffer(render.framebufferWidth, render.framebufferHeight, false, Minecraft.IS_RUNNING_ON_MAC);
	}

	public void prepFramebufferSize() {
		MainWindow window = mc.getWindow();
		if (framebuffer.framebufferWidth != window.getFramebufferWidth()
				|| framebuffer.framebufferHeight != window.getFramebufferHeight()) {
			framebuffer.func_216491_a(window.getFramebufferWidth(), window.getFramebufferHeight(),
					Minecraft.IS_RUNNING_ON_MAC);
		}
	}


}
