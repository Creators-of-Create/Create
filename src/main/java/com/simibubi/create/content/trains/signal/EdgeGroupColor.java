package com.simibubi.create.content.trains.signal;

import net.createmod.catnip.theme.Color;
import net.minecraft.util.FastColor;

public enum EdgeGroupColor {

	YELLOW(0xEBC255, 0xAD7F4D),
	GREEN(0x51C054, 0x538E47),
	BLUE(0x5391E1, 0x41717A),
	ORANGE(0xE36E36, 0xA54436),
	LAVENDER(0xCB92BA, 0x9A7697),
	RED(0xA43538, 0x783141),
	CYAN(0x6EDAD9, 0x78574A),
	BROWN(0xA17C58, 0x78574A),

	WHITE(0xE5E1DC, 0xB2AEAC);

	private Color color;
	private Color darkerColor;
	private int mask;

	private EdgeGroupColor(int color, int darkerColor) {
		this.color = new Color(color);
		this.darkerColor = new Color(darkerColor);
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

	public Color getDarker() {
		return darkerColor;
	}

	public int getBGR() {
		return FastColor.ABGR32.color(0xFF, color.getBlue(), color.getGreen(), color.getRed());
	}

	public int getDarkerBGR() {
		return FastColor.ABGR32.color(0xFF, darkerColor.getBlue(), darkerColor.getGreen(), darkerColor.getRed());
	}

	public static EdgeGroupColor getDefault() {
		return values()[0];
	}

	public static EdgeGroupColor findNextAvailable(int mask) {
		EdgeGroupColor[] values = values();
		for (EdgeGroupColor value : values) {
			if ((mask & 1) == 0)
				return value;
			mask = mask >> 1;
		}
		return WHITE;
	}

}
