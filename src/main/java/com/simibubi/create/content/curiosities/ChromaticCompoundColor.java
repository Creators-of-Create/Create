package com.simibubi.create.content.curiosities;

import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class ChromaticCompoundColor implements IItemColor {

	@Override
	public int getColor(ItemStack stack, int layer) {
		Minecraft mc = Minecraft.getInstance();
		float pt = AnimationTickHolder.getPartialTicks();
		float progress = (float) ((mc.player.getViewYRot(pt)) / 180 * Math.PI) + (AnimationTickHolder.getRenderTime() / 10f);
		if (layer == 0)
			return Color.mixColors(0x6e5773, 0x6B3074, ((float) MathHelper.sin(progress) + 1) / 2);
		if (layer == 1)
			return Color.mixColors(0xd45d79, 0x6e5773,
				((float) MathHelper.sin((float) (progress + Math.PI)) + 1) / 2);
		if (layer == 2)
			return Color.mixColors(0xea9085, 0xd45d79,
				((float) MathHelper.sin((float) (progress * 1.5f + Math.PI)) + 1) / 2);
		return 0;
	}
}
