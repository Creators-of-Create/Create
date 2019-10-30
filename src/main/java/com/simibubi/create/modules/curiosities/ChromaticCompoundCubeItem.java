package com.simibubi.create.modules.curiosities;

import com.simibubi.create.foundation.item.IItemWithColorHandler;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.ColorHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ChromaticCompoundCubeItem extends Item implements IItemWithColorHandler {

	@OnlyIn(value = Dist.CLIENT)
	public static class Color implements IItemColor {
		@Override
		public int getColor(ItemStack stack, int layer) {
			Minecraft mc = Minecraft.getInstance();
			float pt = mc.getRenderPartialTicks();
			float progress = (float) ((mc.player.getYaw(pt)) / 180 * Math.PI)
					+ (AnimationTickHolder.getRenderTick() * 1f);
			if (layer == 0)
				return ColorHelper.mixColors(0xDDDDDD, 0xDDDDDD, ((float) MathHelper.sin(progress) + 1) / 2);
			if (layer == 1)
				return ColorHelper.mixColors(0x72A498, 0xB9D6FF,
						((float) MathHelper.sin((float) (progress + Math.PI)) + 1) / 2);
			if (layer == 2)
				return ColorHelper.mixColors(0x5082CE, 0x91C5B7,
						((float) MathHelper.sin((float) (progress * 1.5f + Math.PI)) + 1) / 2);
			return 0;
		}
	}

	public ChromaticCompoundCubeItem(Properties properties) {
		super(properties);
	}

	@Override
	@OnlyIn(value = Dist.CLIENT)
	public IItemColor getColorHandler() {
		return new Color();
	}

}
