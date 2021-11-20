package com.simibubi.create.compat.jei;

import java.util.function.Supplier;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.element.GuiGameElement;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import net.minecraft.world.item.ItemStack;

public class DoubleItemIcon implements Renderer {

	private Supplier<ItemStack> primarySupplier;
	private Supplier<ItemStack> secondarySupplier;
	private ItemStack primaryStack;
	private ItemStack secondaryStack;
	private Point pos;

	public DoubleItemIcon(Supplier<ItemStack> primary, Supplier<ItemStack> secondary) {
		this.primarySupplier = primary;
		this.secondarySupplier = secondary;
	}

//	@Override
//	public int getWidth() {
//		return 18;
//	}
//
//	@Override
//	public int getHeight() {
//		return 18;
//	}

	public DoubleItemIcon setPos(Point pos) {
		this.pos = pos;
		return this;
	}

	@Override
	public void render(PoseStack matrixStack, Rectangle bounds, int mouseX, int mouseY, float delta) {
		if (primaryStack == null) {
			primaryStack = primarySupplier.get();
			secondaryStack = secondarySupplier.get();
		}

		RenderSystem.enableDepthTest();
		matrixStack.pushPose();
		if(pos == null)
			matrixStack.translate(bounds.getCenterX() - 9, bounds.getCenterY() - 9, getZ());
		else
			matrixStack.translate(pos.getX(), pos.getY(), 0);

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
		RenderSystem.enableBlend();
	}

	@Override
	public int getZ() {
		return 0;
	}

	@Override
	public void setZ(int i) {

	}
}
