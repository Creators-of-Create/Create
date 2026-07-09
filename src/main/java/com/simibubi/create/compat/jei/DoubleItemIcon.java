package com.simibubi.create.compat.jei;

import com.simibubi.create.foundation.render.LegacyRenderSystemBridge;
import mezz.jei.api.gui.drawable.IDrawable;
import net.createmod.catnip.api.client.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import org.joml.Matrix3x2fStack;

import java.util.function.Supplier;

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
	public void draw(GuiGraphicsExtractor graphics, int xOffset, int yOffset) {
		Matrix3x2fStack matrixStack = graphics.pose();
		if (primaryStack == null) {
			primaryStack = primarySupplier.get();
			secondaryStack = secondarySupplier.get();
		}

		LegacyRenderSystemBridge.enableDepthTest();
		matrixStack.pushMatrix();
		matrixStack.translate(xOffset, yOffset);

		matrixStack.pushMatrix();
		matrixStack.translate(1, 1);
		GuiGameElement.of(primaryStack)
			.render(graphics, 0, 0, 0);
		matrixStack.popMatrix();

		matrixStack.pushMatrix();
		matrixStack.translate(10, 10);
		matrixStack.scale(.5f, .5f);
		GuiGameElement.of(secondaryStack)
			.render(graphics, 0, 0, 0);
		matrixStack.popMatrix();

		matrixStack.popMatrix();
	}

}
