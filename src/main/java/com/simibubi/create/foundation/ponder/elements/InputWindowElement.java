package com.simibubi.create.foundation.ponder.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.ponder.PonderLocalization;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.PonderUI;
import com.simibubi.create.foundation.ponder.content.PonderPalette;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class InputWindowElement extends AnimatedOverlayElement {

	private Pointing direction;
	String key;
	AllIcons icon;
	ItemStack item = ItemStack.EMPTY;
	private Vec3d sceneSpace;

	public InputWindowElement clone() {
		InputWindowElement inputWindowElement = new InputWindowElement(sceneSpace, direction);
		inputWindowElement.key = key;
		inputWindowElement.icon = icon;
		inputWindowElement.item = item.copy();
		return inputWindowElement;
	}

	public InputWindowElement(Vec3d sceneSpace, Pointing direction) {
		this.sceneSpace = sceneSpace;
		this.direction = direction;
	}

	public InputWindowElement withItem(ItemStack stack) {
		item = stack;
		return this;
	}

	public InputWindowElement withWrench() {
		item = AllItems.WRENCH.asStack();
		return this;
	}

	public InputWindowElement scroll() {
		icon = AllIcons.I_SCROLL;
		return this;
	}

	public InputWindowElement rightClick() {
		icon = AllIcons.I_RMB;
		return this;
	}
	
	public InputWindowElement showing(AllIcons icon) {
		this.icon = icon;
		return this;
	}

	public InputWindowElement leftClick() {
		icon = AllIcons.I_LMB;
		return this;
	}

	public InputWindowElement whileSneaking() {
		key = "sneak_and";
		return this;
	}

	public InputWindowElement whileCTRL() {
		key = "ctrl_and";
		return this;
	}

	@Override
	protected void render(PonderScene scene, PonderUI screen, MatrixStack ms, float partialTicks, float fade) {
		FontRenderer font = screen.getFontRenderer();
		int width = 0;
		int height = 0;

		int xFade = direction == Pointing.RIGHT ? -1 : direction == Pointing.LEFT ? 1 : 0;
		int yFade = direction == Pointing.DOWN ? -1 : direction == Pointing.UP ? 1 : 0;
		xFade *= 10 * (1 - fade);
		yFade *= 10 * (1 - fade);

		boolean hasItem = !item.isEmpty();
		boolean hasText = key != null;
		boolean hasIcon = icon != null;
		int keyWidth = 0;
		String text = hasText ? PonderLocalization.getShared(key) : "";

		if (fade < 1 / 16f)
			return;
		Vec2f sceneToScreen = scene.getTransform()
			.sceneToScreen(sceneSpace, partialTicks);

		if (hasIcon) {
			width += 24;
			height = 24;
		}

		if (hasText) {
			keyWidth = font.getStringWidth(text);
			width += keyWidth;
		}

		if (hasItem) {
			width += 24;
			height = 24;
		}

		RenderSystem.pushMatrix();
		RenderSystem.translated(sceneToScreen.x + xFade, sceneToScreen.y + yFade, 400);

		PonderUI.renderSpeechBox(0, 0, width, height, false, direction, true);

		if (hasText)
			font.drawString(text, 2, (height - font.FONT_HEIGHT) / 2 + 2,
				ColorHelper.applyAlpha(PonderPalette.WHITE.getColor(), fade));

		if (hasIcon) {
			RenderSystem.pushMatrix();
			RenderSystem.translated(keyWidth, 0, 0);
			RenderSystem.scaled(1.5, 1.5, 1.5);
			icon.draw(0, 0);
			RenderSystem.popMatrix();
		}

		if (hasItem) {
			GuiGameElement.of(item)
				.at(keyWidth + (hasIcon ? 24 : 0), 0)
				.scale(1.5)
				.render();
			RenderSystem.disableDepthTest();
		}

		RenderSystem.popMatrix();
	}

}
