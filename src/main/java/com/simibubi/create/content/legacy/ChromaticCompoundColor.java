package com.simibubi.create.content.legacy;

import net.createmod.catnip.utility.AnimationTickHolder;
import net.createmod.catnip.utility.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class ChromaticCompoundColor implements ItemColor {

	@Override
	public int getColor(ItemStack stack, int layer) {
		Minecraft mc = Minecraft.getInstance();
		float pt = AnimationTickHolder.getPartialTicks();
		float progress = (float) ((mc.player.getViewYRot(pt)) / 180 * Math.PI) + (AnimationTickHolder.getRenderTime() / 10f);
		if (layer == 0)
			return Color.mixColors(0x6e5773, 0x6B3074, ((float) Mth.sin(progress) + 1) / 2);
		if (layer == 1)
			return Color.mixColors(0xd45d79, 0x6e5773,
				((float) Mth.sin((float) (progress + Math.PI)) + 1) / 2);
		if (layer == 2)
			return Color.mixColors(0xea9085, 0xd45d79,
				((float) Mth.sin((float) (progress * 1.5f + Math.PI)) + 1) / 2);
		return 0;
	}
}
