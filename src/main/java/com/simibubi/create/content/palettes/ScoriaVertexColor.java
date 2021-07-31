package com.simibubi.create.content.palettes;

import com.simibubi.create.foundation.block.render.IBlockVertexColor;
import com.simibubi.create.foundation.utility.Color;

public class ScoriaVertexColor implements IBlockVertexColor {

	@Override
	public int getColor(float x, float y, float z) {
		float x2 = (float) Math.floor(z + x - y * .5);
		float y2 = (float) Math.floor(y * 1.5 + x * .5 - z);
		float z2 = (float) Math.floor(y - z * .5 - x);

		Color color = new Color(0x448888);
		if (x2 % 2 == 0)
			color.modifyValue(v -> v | 0x0011ff);
		if (z2 % 2 == 0)
			color.modifyValue(v -> v | 0x888888);

		color.mixWith(Color.rainbowColor((int) (x + y + z) * 170), .4f);

		if ((x2 % 4 == 0) || (y2 % 4 == 0))
			color.mixWith(Color.WHITE, .8f);

		return color.getRGB() & 0x00_ffffff;
	}

}
