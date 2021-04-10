package com.simibubi.create.foundation.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;

import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

public abstract class StencilElement implements IScreenRenderable {

	protected int width = 0;
	protected int height = 0;
	float x, y , z;

	public <T extends StencilElement> T at(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		//noinspection unchecked
		return (T) this;
	}

	public <T extends StencilElement> T withBounds(int width, int height) {
		this.width = width;
		this.height = height;
		//noinspection unchecked
		return (T) this;
	}

	@Override
	public void draw(MatrixStack ms, AbstractGui screen, int x, int y) {
		this.at(x, y, 0).render(ms);
	}

	@Override
	public void draw(MatrixStack ms, int x, int y) {
		this.at(x, y, 0).render(ms);
	}

	public void render(MatrixStack ms) {
		ms.push();
		transform(ms);
		prepareStencil(ms);
		renderStencil(ms);
		prepareElement(ms);
		renderElement(ms);
		cleanUp(ms);
		ms.pop();
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	protected abstract void renderStencil(MatrixStack ms);

	protected abstract void renderElement(MatrixStack ms);

	protected void transform(MatrixStack ms) {
		ms.translate(x, y, z);
	}

	protected void prepareStencil(MatrixStack ms) {
		GL11.glDisable(GL11.GL_STENCIL_TEST);
		RenderSystem.stencilMask(~0);
		RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, Minecraft.IS_RUNNING_ON_MAC);
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
