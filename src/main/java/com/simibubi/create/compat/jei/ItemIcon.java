package com.simibubi.create.compat.jei;

import com.simibubi.create.foundation.render.LegacyRenderSystemBridge;
import mezz.jei.api.gui.drawable.IDrawable;
import net.createmod.catnip.api.client.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import org.joml.Matrix3x2fStack;

import java.util.function.Supplier;

public class ItemIcon implements IDrawable {

	private Supplier<ItemStack> supplier;
	private ItemStack stack;

	public ItemIcon(Supplier<ItemStack> stack) {
		this.supplier = stack;
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
		if (stack == null) {
			stack = supplier.get();
		}

		LegacyRenderSystemBridge.enableDepthTest();
		matrixStack.pushMatrix();
		matrixStack.translate(xOffset + 1, yOffset + 1);

		GuiGameElement.of(stack)
			.render(graphics, 0, 0, 0);

		matrixStack.popMatrix();
	}


}
