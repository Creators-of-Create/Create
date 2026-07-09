package com.simibubi.create.content.legacy;

import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.theme.Color;
import net.minecraft.client.Minecraft;
import com.simibubi.create.foundation.item.ItemColor;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class ChromaticCompoundColor implements ItemColor {

	@Override
	public int getColor(ItemStack stack, int layer) {
		Minecraft mc = Minecraft.getInstance();
		float pt = AnimationTickHolder.getPartialTicks();
		float progress = (float) ((mc.player.getViewYRot(pt)) / 180 * Math.PI) + (AnimationTickHolder.getRenderTime() / 10f);
		if (layer == 0)
			return Color.mixColors(
				ARGB.color(110, 87, 115),
				ARGB.color(107, 48, 116),
				(Mth.sin(progress) + 1) / 2
			);
		if (layer == 1)
			return Color.mixColors(
				ARGB.color(212, 93, 121),
				ARGB.color(110, 87, 115),
				(Mth.sin((float) (progress + Math.PI)) + 1) / 2
			);
		if (layer == 2)
			return Color.mixColors(
				ARGB.color(234, 144, 133),
				ARGB.color(212, 93, 121),
				(Mth.sin((float) (progress * 1.5f + Math.PI)) + 1) / 2
			);
		return 0;
	}
}
