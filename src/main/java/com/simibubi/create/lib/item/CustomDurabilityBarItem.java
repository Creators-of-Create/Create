package com.simibubi.create.lib.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.simibubi.create.lib.mixin.accessor.ItemRendererAccessor;
import com.simibubi.create.lib.util.DurabilityBarUtil;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public interface CustomDurabilityBarItem extends CustomGuiOverlayItem {
	default boolean renderOverlay(ItemStack stack, int x, int y, Font textRenderer, ItemRenderer itemRenderer) {
		if (showDurabilityBar(stack)) {
			RenderSystem.disableDepthTest();
			RenderSystem.disableTexture();
			//RenderSystem.disableAlphaTest();
			RenderSystem.disableBlend();

			BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();

			int width = (int) Math.round(13.0D - Mth.clamp(getDurabilityForDisplay(stack), 0, 1) * 13.0D);
			int color = getRGBDurabilityForDisplay(stack);

			((ItemRendererAccessor) itemRenderer).create$fillRect(bufferBuilder, x + 2, y + 13, 13, 2, 0, 0, 0, 255);
			((ItemRendererAccessor) itemRenderer).create$fillRect(bufferBuilder, x + 2, y + 13, width, 1, color >> 16 & 255, color >> 8 & 255, color & 255, 255);

			RenderSystem.enableBlend();
			//RenderSystem.enableAlphaTest();
			RenderSystem.enableTexture();
			RenderSystem.enableDepthTest();

			return true;
		}
		return false;
	}

	default boolean showDurabilityBar(ItemStack stack) {
		return DurabilityBarUtil.showDurabilityBarDefault(stack);
	}

	default double getDurabilityForDisplay(ItemStack stack) {
		return DurabilityBarUtil.getDurabilityForDisplayDefault(stack);
	}

	default int getRGBDurabilityForDisplay(ItemStack stack) {
		return DurabilityBarUtil.getRGBDurabilityForDisplayDefault(stack);
	}
}
