package com.simibubi.create.content.palettes;

import com.simibubi.create.foundation.block.IBlockVertexColor;
import com.simibubi.create.foundation.utility.ColorHelper;

public class ScoriaVertexColor implements IBlockVertexColor {

	@Override
	public int getColor(float x, float y, float z) {
		float x2 = (float) Math.floor(z + x - y * .5);
		float y2 = (float) Math.floor(y * 1.5 + x * .5 - z);
		float z2 = (float) Math.floor(y - z * .5 - x);

		int color = 0x448888;
		if (x2 % 2 == 0)
			color |= 0x0011ff;
		if (z2 % 2 == 0)
			color |= 0x888888;
		color = ColorHelper.mixColors(ColorHelper.rainbowColor((int) (x + y + z) * 170), color, .6f);
		if ((x2 % 4 == 0) || (y2 % 4 == 0))
			color = ColorHelper.mixColors(0xffffff, color, .2f);
		return color;
	}

}
