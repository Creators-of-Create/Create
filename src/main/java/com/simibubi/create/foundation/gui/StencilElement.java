package com.simibubi.create.foundation.gui;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;

public abstract class StencilElement extends RenderElement {

	@Override
	public void render(MatrixStack ms) {
		ms.pushPose();
		transform(ms);
		prepareStencil(ms);
		renderStencil(ms);
		prepareElement(ms);
		renderElement(ms);
		cleanUp(ms);
		ms.popPose();
	}

	protected abstract void renderStencil(MatrixStack ms);

	protected abstract void renderElement(MatrixStack ms);

	protected void transform(MatrixStack ms) {
		ms.translate(x, y, z);
	}

	protected void prepareStencil(MatrixStack ms) {
		GL11.glDisable(GL11.GL_STENCIL_TEST);
		RenderSystem.stencilMask(~0);
		RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, Minecraft.ON_OSX);
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		RenderSystem.stencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP);
		RenderSystem.stencilMask(0xFF);
		RenderSystem.stencilFunc(GL11.GL_NEVER, 1, 0xFF);
	}

	protected void prepareElement(MatrixStack ms) {
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
		RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
	}

	protected void cleanUp(MatrixStack ms) {
		GL11.glDisable(GL11.GL_STENCIL_TEST);

	}
}
