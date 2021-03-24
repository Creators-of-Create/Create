package com.simibubi.create.foundation.ponder.elements;

import java.util.List;
import java.util.function.Supplier;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.ponder.PonderLocalization;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.PonderUI;
import com.simibubi.create.foundation.ponder.content.PonderPalette;
import com.simibubi.create.foundation.utility.ColorHelper;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.Style;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class TextWindowElement extends AnimatedOverlayElement {

	Supplier<String> textGetter = () -> "(?) No text was provided";
	String bakedText;

	// from 0 to 200
	int y;

	Vector3d vec;

	boolean nearScene = false;
	int color = PonderPalette.WHITE.getColor();

	public class Builder {

		private PonderScene scene;

		public Builder(PonderScene scene) {
			this.scene = scene;
		}

		public Builder colored(PonderPalette color) {
			TextWindowElement.this.color = color.getColor();
			return this;
		}

		public Builder pointAt(Vector3d vec) {
			TextWindowElement.this.vec = vec;
			return this;
		}

		public Builder independent(int y) {
			TextWindowElement.this.y = y;
			return this;
		}

		public Builder independent() {
			return independent(0);
		}

		public Builder text(String defaultText) {
			textGetter = scene.registerText(defaultText);
			return this;
		}

		public Builder sharedText(String key) {
			textGetter = () -> PonderLocalization.getShared(key);
			return this;
		}

		public Builder placeNearTarget() {
			TextWindowElement.this.nearScene = true;
			return this;
		}

		public Builder attachKeyFrame() {
			scene.builder()
				.addLazyKeyframe();
			return this;
		}

	}

	@Override
	protected void render(PonderScene scene, PonderUI screen, MatrixStack ms, float partialTicks, float fade) {
		if (bakedText == null)
			bakedText = textGetter.get();
		if (fade < 1 / 16f)
			return;
		Vector2f sceneToScreen = vec != null ? scene.getTransform()
			.sceneToScreen(vec, partialTicks) : new Vector2f(screen.width / 2, (screen.height - 200) / 2 + y - 8);

		float yDiff = (screen.height / 2f - sceneToScreen.y - 10) / 100f;
		int targetX = (int) (screen.width * MathHelper.lerp(yDiff * yDiff, 6f / 8, 5f / 8));

		if (nearScene)
			targetX = (int) Math.min(targetX, sceneToScreen.x + 50);

		int textWidth = Math.min(screen.width - targetX, 180);

		List<ITextProperties> lines = screen.getFontRenderer().getTextHandler().wrapLines(bakedText, textWidth, Style.EMPTY);

		int boxWidth = 0;
		for (ITextProperties line : lines)
			boxWidth = Math.max(boxWidth, screen.getFontRenderer().getWidth(line));

		int boxHeight = screen.getFontRenderer()
			.getWordWrappedHeight(bakedText, boxWidth);

		ms.push();
		ms.translate(0, sceneToScreen.y, 400);

		PonderUI.renderBox(ms, targetX - 10, 3, boxWidth, boxHeight - 1, 0xaa000000, 0x30eebb00, 0x10eebb00);

		int brighterColor = ColorHelper.mixAlphaColors(color, 0xFFffffdd, 1 / 2f);
		if (vec != null) {
			ms.push();
			ms.translate(sceneToScreen.x, 0, 0);
			double lineTarget = (targetX - sceneToScreen.x) * fade;
			ms.scale((float) lineTarget, 1, 1);
			Matrix4f model = ms.peek().getModel();
			GuiUtils.drawGradientRect(model, -100, 0, 0, 1, 1, brighterColor, brighterColor);
			GuiUtils.drawGradientRect(model, -100, 0, 1, 1, 2, 0xFF494949, 0xFF393939);
			ms.pop();
		}

		for (int i = 0; i < lines.size(); i++) {
			ITextProperties s = lines.get(i);
			// FIXME: Probably completely broken
			screen.getFontRenderer()
				.drawTrimmed(s, 20, targetX - 10, 3 + 9 * i, ColorHelper.applyAlpha(brighterColor, fade));
		}
		ms.pop();
	}

	public int getColor() {
		return color;
	}

}
