package com.simibubi.create.compat.jei;

import java.util.function.Supplier;

import com.mojang.blaze3d.systems.RenderSystem;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class DoubleItemIcon implements IDrawable {

	private Supplier<ItemStack> primarySupplier;
	private Supplier<ItemStack> secondarySupplier;
	private ItemStack primaryStack;
	private ItemStack secondaryStack;

	public DoubleItemIcon(Supplier<ItemStack> primary, Supplier<ItemStack> secondary) {
		this.primarySupplier = primary;
		this.secondarySupplier = secondary;
	}

	@Override
	public int getWidth() {
		return 18;
	}

	@Override
	public int getHeight() {
		return 18;
	}

	@Override
	public void draw(int xOffset, int yOffset) {
		if (primaryStack == null) {
			primaryStack = primarySupplier.get();
			secondaryStack = secondarySupplier.get();
		}
		
		RenderHelper.enable();
		RenderSystem.color4f(1, 1, 1, 1);
		RenderSystem.enableDepthTest();
		RenderSystem.pushMatrix();
		RenderSystem.translated(xOffset, yOffset, 0);

		RenderSystem.pushMatrix();
		RenderSystem.translated(1, 1, 0);
		Minecraft.getInstance().getItemRenderer().renderItemIntoGUI(primaryStack, 0, 0);
		RenderSystem.popMatrix();

		RenderSystem.pushMatrix();
		RenderSystem.translated(10, 10, 100);
		RenderSystem.scaled(.5, .5, .5);
		Minecraft.getInstance().getItemRenderer().renderItemIntoGUI(secondaryStack, 0, 0);
		RenderSystem.popMatrix();

		RenderSystem.popMatrix();
		RenderSystem.enableBlend();
	}

}
