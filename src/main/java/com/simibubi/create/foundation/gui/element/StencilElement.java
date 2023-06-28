package com.simibubi.create.foundation.gui.element;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public abstract class StencilElement extends RenderElement {

	@Override
	public void render(GuiGraphics graphics) {
		PoseStack ms = graphics.pose();
		ms.pushPose();
		transform(ms);
		prepareStencil(ms);
		renderStencil(graphics);
		prepareElement(ms);
		renderElement(graphics);
		cleanUp(ms);
		ms.popPose();
	}

	protected abstract void renderStencil(GuiGraphics graphics);

	protected abstract void renderElement(GuiGraphics graphics);

	protected void transform(PoseStack ms) {
		ms.translate(x, y, z);
	}

	protected void prepareStencil(PoseStack ms) {
		GL11.glDisable(GL11.GL_STENCIL_TEST);
		RenderSystem.stencilMask(~0);
		RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, Minecraft.ON_OSX);
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		RenderSystem.stencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP);
		RenderSystem.stencilMask(0xFF);
		RenderSystem.stencilFunc(GL11.GL_NEVER, 1, 0xFF);
	}

	protected void prepareElement(PoseStack ms) {
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
		RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
	}

	protected void cleanUp(PoseStack ms) {
		GL11.glDisable(GL11.GL_STENCIL_TEST);

	}
}
