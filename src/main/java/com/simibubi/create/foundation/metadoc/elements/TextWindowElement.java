package com.simibubi.create.foundation.metadoc.elements;

import java.util.List;
import java.util.function.Supplier;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.metadoc.MetaDocScene;
import com.simibubi.create.foundation.metadoc.MetaDocScreen;
import com.simibubi.create.foundation.utility.ColorHelper;

import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class TextWindowElement extends AnimatedOverlayElement {

	Supplier<String> textGetter;
	String bakedText;
	Vec3d vec;

	public TextWindowElement(Supplier<String> textGetter) {
		this.textGetter = textGetter;
	}

	public TextWindowElement moveTo(Vec3d vec) {
		this.vec = vec;
		return this;
	}

	@Override
	protected void render(MetaDocScene scene, MetaDocScreen screen, MatrixStack ms, float partialTicks, float fade) {
		if (bakedText == null)
			bakedText = textGetter.get();
		if (fade < 1 / 16f)
			return;
		Vec2f sceneToScreen = scene.getTransform()
			.sceneToScreen(vec);
		int targetX = screen.width * 6 / 8;
		int textWidth = screen.width - targetX;
		
		List<String> list = screen.getFontRenderer()
			.listFormattedStringToWidth(bakedText, textWidth);
		int boxWidth = 0;
		for (String string : list)
			boxWidth = Math.max(boxWidth, screen.getFontRenderer()
				.getStringWidth(string));
		int boxHeight = screen.getFontRenderer()
			.getWordWrappedHeight(bakedText, textWidth);

		RenderSystem.pushMatrix();
		RenderSystem.translatef(0, sceneToScreen.y, 400);

		screen.renderBox(targetX - 10, 3, boxWidth, boxHeight -1 , 0x55000000, 0x30eebb00, 0x10eebb00);

		RenderSystem.pushMatrix();
		RenderSystem.translatef(sceneToScreen.x, 0, 0);
		double lineTarget = (targetX - sceneToScreen.x) * fade;
		RenderSystem.scaled(lineTarget, 1, 1);
		GuiUtils.drawGradientRect(-100, 0, 0, 1, 1, 0xFFFFFFFF, 0xFFFFFFFF);
		GuiUtils.drawGradientRect(-100, 0, 1, 1, 2, 0xFF494949, 0xFF393939);
		RenderSystem.popMatrix();

		screen.getFontRenderer()
			.drawSplitString(bakedText, targetX - 10, 3, textWidth, ColorHelper.applyAlpha(0xeeeeee, fade));
		RenderSystem.popMatrix();
	}

}
