package com.simibubi.create.foundation.ponder.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.Theme;
import com.simibubi.create.foundation.gui.element.BoxElement;
import com.simibubi.create.foundation.gui.widget.AbstractSimiWidget;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.sounds.SoundManager;

public class PonderProgressBar extends AbstractSimiWidget {

	LerpedFloat progress;

	PonderUI ponder;

	public PonderProgressBar(PonderUI ponder, int xIn, int yIn, int widthIn, int heightIn) {
		super(xIn, yIn, widthIn, heightIn);

		this.ponder = ponder;
		progress = LerpedFloat.linear()
				.startWithValue(0);
	}

	public void tick() {
		progress.chase(ponder.getActiveScene()
				.getSceneProgress(), .5f, LerpedFloat.Chaser.EXP);
		progress.tickChaser();
	}

	@Override
	protected boolean clicked(double mouseX, double mouseY) {
		return this.active && this.visible && ponder.getActiveScene().getKeyframeCount() > 0
				&& mouseX >= (double) this.getX() && mouseX < (double) (this.getX() + this.width + 4) && mouseY >= (double) this.getY() - 3
				&& mouseY < (double) (this.getY() + this.height + 20);
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		PonderScene activeScene = ponder.getActiveScene();

		int keyframeIndex = getHoveredKeyframeIndex(activeScene, mouseX);

		if (keyframeIndex == -1)
			ponder.seekToTime(0);
		else if (keyframeIndex == activeScene.getKeyframeCount())
			ponder.seekToTime(activeScene.getTotalTime());
		else
			ponder.seekToTime(activeScene.getKeyframeTime(keyframeIndex));
	}

	public int getHoveredKeyframeIndex(PonderScene activeScene, double mouseX) {
		int totalTime = activeScene.getTotalTime();
		int clickedAtTime = (int) ((mouseX - getX()) / ((double) width + 4) * totalTime);

		{
			int lastKeyframeTime = activeScene.getKeyframeTime(activeScene.getKeyframeCount() - 1);

			int diffToEnd = totalTime - clickedAtTime;
			int diffToLast = clickedAtTime - lastKeyframeTime;

			if (diffToEnd > 0 && diffToEnd < diffToLast / 2) {
				return activeScene.getKeyframeCount();
			}
		}

		int index = -1;

		for (int i = 0; i < activeScene.getKeyframeCount(); i++) {
			int keyframeTime = activeScene.getKeyframeTime(i);

			if (keyframeTime > clickedAtTime)
				break;

			index = i;
		}

		return index;
	}

	@Override
	public void renderButton(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		PoseStack ms = graphics.pose();
		
		isHovered = clicked(mouseX, mouseY);

		new BoxElement()
				.withBackground(Theme.c(Theme.Key.PONDER_BACKGROUND_FLAT))
				.gradientBorder(Theme.p(Theme.Key.PONDER_IDLE))
				.at(getX(), getY(), 400)
				.withBounds(width, height)
				.render(graphics);

		ms.pushPose();
		ms.translate(getX() - 2, getY() - 2, 100);

		ms.pushPose();
		ms.scale((width + 4) * progress.getValue(partialTicks), 1, 1);
		int c1 = Theme.i(Theme.Key.PONDER_PROGRESSBAR, true);
		int c2 = Theme.i(Theme.Key.PONDER_PROGRESSBAR, false);
		graphics.fillGradient(0, 3, 1, 4, 310, c1, c1);
		graphics.fillGradient(0, 4, 1, 5, 310, c2, c2);
		ms.popPose();

		renderKeyframes(graphics, mouseX, partialTicks);

		ms.popPose();
	}

	private void renderKeyframes(GuiGraphics graphics, int mouseX, float partialTicks) {
		PonderScene activeScene = ponder.getActiveScene();

		int hoverStartColor = Theme.i(Theme.Key.PONDER_HOVER, true) | 0xa0_000000;
		int hoverEndColor = Theme.i(Theme.Key.PONDER_HOVER, false) | 0xa0_000000;
		int idleStartColor = Theme.i(Theme.Key.PONDER_IDLE, true) | 0x40_000000;
		int idleEndColor = Theme.i(Theme.Key.PONDER_IDLE, false) | 0x40_000000;
		int hoverIndex;

		if (isHovered) {
			hoverIndex = getHoveredKeyframeIndex(activeScene, mouseX);
		} else {
			hoverIndex = -2;
		}

		if (hoverIndex == -1)
			drawKeyframe(graphics, activeScene, true, 0, 0, hoverStartColor, hoverEndColor, 8);
		else if (hoverIndex == activeScene.getKeyframeCount())
			drawKeyframe(graphics, activeScene, true, activeScene.getTotalTime(), width + 4, hoverStartColor, hoverEndColor, 8);

		for (int i = 0; i < activeScene.getKeyframeCount(); i++) {
			int keyframeTime = activeScene.getKeyframeTime(i);
			int keyframePos = (int) (((float) keyframeTime) / ((float) activeScene.getTotalTime()) * (width + 4));

			boolean selected = i == hoverIndex;
			int startColor = selected ? hoverStartColor : idleStartColor;
			int endColor = selected ? hoverEndColor : idleEndColor;
			int height = selected ? 8 : 4;

			drawKeyframe(graphics, activeScene, selected, keyframeTime, keyframePos, startColor, endColor, height);

		}
	}

	private void drawKeyframe(GuiGraphics graphics, PonderScene activeScene, boolean selected, int keyframeTime,
		int keyframePos, int startColor, int endColor, int height) {
		PoseStack ms = graphics.pose();
		if (selected) {
			Font font = Minecraft.getInstance().font;
			graphics.fillGradient(keyframePos, 10, keyframePos + 1, 10 + height, 600, endColor, startColor);
			ms.pushPose();
			ms.translate(0, 0, 200);
			String text;
			int offset;
			if (activeScene.getCurrentTime() < keyframeTime) {
				text = ">";
				offset = -1 - font.width(text);
			} else {
				text = "<";
				offset = 3;
			}
			graphics.drawString(font, text, keyframePos + offset, 10, endColor, false);
			ms.popPose();
		}

		graphics.fillGradient(keyframePos, -1, keyframePos + 1, 2 + height, 400, startColor, endColor);
	}

	@Override
	public void playDownSound(SoundManager handler) {

	}
}
