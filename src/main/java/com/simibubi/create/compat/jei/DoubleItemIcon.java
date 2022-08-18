package com.simibubi.create.compat.jei;

import java.util.function.Supplier;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.element.GuiGameElement;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.world.item.ItemStack;

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
	public void draw(PoseStack matrixStack, int xOffset, int yOffset) {
		if (primaryStack == null) {
			primaryStack = primarySupplier.get();
			secondaryStack = secondarySupplier.get();
		}

		RenderSystem.enableDepthTest();
		matrixStack.pushPose();
		matrixStack.translate(xOffset, yOffset, 0);

		matrixStack.pushPose();
		matrixStack.translate(1, 1, 0);
		GuiGameElement.of(primaryStack)
			.render(matrixStack);
		matrixStack.popPose();

		matrixStack.pushPose();
		matrixStack.translate(10, 10, 100);
		matrixStack.scale(.5f, .5f, .5f);
		GuiGameElement.of(secondaryStack)
			.render(matrixStack);
		matrixStack.popPose();

		matrixStack.popPose();
	}

}
