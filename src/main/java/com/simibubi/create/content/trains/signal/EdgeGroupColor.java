package com.simibubi.create.content.trains.signal;

import net.createmod.catnip.utility.theme.Color;

public enum EdgeGroupColor {

	YELLOW(0xEBC255),
	GREEN(0x51C054),
	BLUE(0x5391E1),
	ORANGE(0xE36E36),
	LAVENDER(0xCB92BA),
	RED(0xA43538),
	CYAN(0x6EDAD9),
	BROWN(0xA17C58),

	WHITE(0xE5E1DC)

	;

	private Color color;
	private int mask;

	private EdgeGroupColor(int color) {
		this.color = new Color(color);
		mask = 1 << ordinal();
	}

	public int strikeFrom(int mask) {
		if (this == WHITE)
			return mask;
		return mask | this.mask;
	}

	public Color get() {
		return color;
	}

	public static EdgeGroupColor getDefault() {
		return values()[0];
	}

	public static EdgeGroupColor findNextAvailable(int mask) {
		EdgeGroupColor[] values = values();
		for (int i = 0; i < values.length; i++) {
			if ((mask & 1) == 0)
				return values[i];
			mask = mask >> 1;
		}
		return WHITE;
	}

}
