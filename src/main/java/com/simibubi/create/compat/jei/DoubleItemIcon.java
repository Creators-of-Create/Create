package com.simibubi.create.compat.jei;

import java.util.function.Supplier;

import com.mojang.blaze3d.matrix.MatrixStack;
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
	public void draw(MatrixStack matrixStack, int xOffset, int yOffset) {
		if (primaryStack == null) {
			primaryStack = primarySupplier.get();
			secondaryStack = secondarySupplier.get();
		}
		
		RenderHelper.enable();
		RenderSystem.color4f(1, 1, 1, 1);
		RenderSystem.enableDepthTest();
		matrixStack.push();
		matrixStack.translate(xOffset, yOffset, 0);

		matrixStack.push();
		matrixStack.translate(1, 1, 0);
		Minecraft.getInstance().getItemRenderer().renderItemIntoGUI(primaryStack, 0, 0);
		matrixStack.pop();

		matrixStack.push();
		matrixStack.translate(10, 10, 100);
		matrixStack.scale(.5f, .5f, .5f);
		Minecraft.getInstance().getItemRenderer().renderItemIntoGUI(secondaryStack, 0, 0);
		matrixStack.pop();

		matrixStack.pop();
		RenderSystem.enableBlend();
	}

}
