package com.simibubi.create.foundation.ponder;

import com.simibubi.create.foundation.utility.Color;

public enum PonderPalette {

	WHITE(0xFF_eeeeee),
	BLACK(0xFF_221111),

	RED(0xFF_ff5d6c),
	GREEN(0xFF_8cba51),
	BLUE(0xFF_5f6caf),

	SLOW(0xFF_22ff22),
	MEDIUM(0xFF_0084ff),
	FAST(0xFF_ff55ff),

	INPUT(0xFF_7FCDE0),
	OUTPUT(0xFF_DDC166),

	;

	private final Color color;

	PonderPalette(int color) {
		this.color = new Color(color);
	}

	public int getColor() {
		return color.getRGB();
	}

	public Color getColorObject() {
		return color;
	}
}
