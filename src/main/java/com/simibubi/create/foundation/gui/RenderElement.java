package com.simibubi.create.foundation.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.AbstractGui;

public abstract class RenderElement implements IScreenRenderable {

	public static RenderElement EMPTY = new RenderElement() {@Override public void render(MatrixStack ms) {}};

	protected int width = 16, height = 16;
	protected float x = 0, y = 0, z = 0;
	protected float alpha = 1f;

	public <T extends RenderElement> T at(float x, float y) {
		this.x = x;
		this.y = y;
		//noinspection unchecked
		return (T) this;
	}

	public <T extends RenderElement> T at(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		//noinspection unchecked
		return (T) this;
	}

	public <T extends RenderElement> T withBounds(int width, int height) {
		this.width = width;
		this.height = height;
		//noinspection unchecked
		return (T) this;
	}

	public <T extends RenderElement> T withAlpha(float alpha) {
		this.alpha = alpha;
		//noinspection unchecked
		return (T) this;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public abstract void render(MatrixStack ms);

	@Override
	public void draw(MatrixStack ms, AbstractGui screen, int x, int y) {
		this.at(x, y).render(ms);
	}

	@Override
	public void draw(MatrixStack ms, int x, int y) {
		this.at(x, y).render(ms);
	}
}
