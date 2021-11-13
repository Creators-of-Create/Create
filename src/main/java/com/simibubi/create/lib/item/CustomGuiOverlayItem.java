package com.simibubi.create.lib.item;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;

public interface CustomGuiOverlayItem {
	/**
	 * Returning true cancels the default durability bar rendering.
	 */
	boolean renderOverlay(ItemStack stack, int x, int y, Font textRenderer, ItemRenderer itemRenderer);
}
