package com.simibubi.create.compat.jei;

import mezz.jei.api.gui.drawable.IDrawable;

public class EmptyBackground implements IDrawable {

	private int width;
	private int height;

	public EmptyBackground(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void draw(int xOffset, int yOffset) {
	}

}
