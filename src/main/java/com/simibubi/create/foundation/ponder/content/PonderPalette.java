package com.simibubi.create.foundation.ponder.content;

public enum PonderPalette {

	WHITE(0xFF_eeeeee),
	BLACK(0xFF_221111),
	
	RED(0xFF_ff5d6c),
	GREEN(0xFF_8cba51),
	BLUE(0xFF_5f6caf),
	
	SLOW(0xFF_22ff22),
	MEDIUM(0xFF_0084ff),
	FAST(0xFF_ff55ff),
	
	;

	private int color;

	private PonderPalette(int color) {
		this.color = color;
	}

	public int getColor() {
		return color;
	}
}
