package com.simibubi.create.foundation.ponder;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.Theme;
import com.simibubi.create.foundation.gui.element.BoxElement;
import com.simibubi.create.foundation.gui.widget.AbstractSimiWidget;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

import com.simibubi.create.lib.utility.GuiUtils;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
		return this.active && this.visible && !ponder.getActiveScene().keyframeTimes.isEmpty()
				&& mouseX >= (double) this.x && mouseX < (double) (this.x + this.width + 4) && mouseY >= (double) this.y - 3
				&& mouseY < (double) (this.y + this.height + 20);
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		PonderScene activeScene = ponder.getActiveScene();
		IntList keyframeTimes = activeScene.keyframeTimes;

		int keyframeIndex = getHoveredKeyframeIndex(activeScene, mouseX);

		if (keyframeIndex == -1)
			ponder.seekToTime(0);
		else if (keyframeIndex == keyframeTimes.size())
			ponder.seekToTime(activeScene.totalTime);
		else
			ponder.seekToTime(keyframeTimes.getInt(keyframeIndex));
	}

	public int getHoveredKeyframeIndex(PonderScene activeScene, double mouseX) {
		IntList keyframeTimes = activeScene.keyframeTimes;

		int totalTime = activeScene.totalTime;
		int clickedAtTime = (int) ((mouseX - x) / ((double) width + 4) * totalTime);

		{
			int lastKeyframeTime = keyframeTimes.getInt(keyframeTimes.size() - 1);

			int diffToEnd = totalTime - clickedAtTime;
			int diffToLast = clickedAtTime - lastKeyframeTime;

			if (diffToEnd > 0 && diffToEnd < diffToLast / 2) {
				return keyframeTimes.size();
			}
		}

		int index = -1;

		for (int i = 0; i < keyframeTimes.size(); i++) {
			int keyframeTime = keyframeTimes.getInt(i);

			if (keyframeTime > clickedAtTime)
				break;

			index = i;
		}

		return index;
	}

	@Override
	public void renderButton(@Nonnull PoseStack ms, int mouseX, int mouseY, float partialTicks) {

		isHovered = clicked(mouseX, mouseY);

		new BoxElement()
				.withBackground(Theme.c(Theme.Key.PONDER_BACKGROUND_FLAT))
				.gradientBorder(Theme.p(Theme.Key.PONDER_IDLE))
				.at(x, y, 300)
				.withBounds(width, height)
				.render(ms);

		ms.pushPose();
		ms.translate(x - 2, y - 2, 200);

		ms.pushPose();
		ms.scale((width + 4) * progress.getValue(partialTicks), 1, 1);
		int c1 = Theme.i(Theme.Key.PONDER_PROGRESSBAR, true);
		int c2 = Theme.i(Theme.Key.PONDER_PROGRESSBAR, false);
		GuiUtils.drawGradientRect(ms.last().pose(), 110, 0, 3, 1, 4, c1, c1);
		GuiUtils.drawGradientRect(ms.last().pose(), 110, 0, 4, 1, 5, c2, c2);
		ms.popPose();

		renderKeyframes(ms, mouseX, partialTicks);

		ms.popPose();
	}

	private void renderKeyframes(PoseStack ms, int mouseX, float partialTicks) {
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
		IntList keyframeTimes = activeScene.keyframeTimes;

		if (hoverIndex == -1)
			drawKeyframe(ms, activeScene, true, 0, 0, hoverStartColor, hoverEndColor, 8);
		else if (hoverIndex == keyframeTimes.size())
			drawKeyframe(ms, activeScene, true, activeScene.totalTime, width + 4, hoverStartColor, hoverEndColor, 8);

		for (int i = 0; i < keyframeTimes.size(); i++) {
			int keyframeTime = keyframeTimes.getInt(i);
			int keyframePos = (int) (((float) keyframeTime) / ((float) activeScene.totalTime) * (width + 4));

			boolean selected = i == hoverIndex;
			int startColor = selected ? hoverStartColor : idleStartColor;
			int endColor = selected ? hoverEndColor : idleEndColor;
			int height = selected ? 8 : 4;

			drawKeyframe(ms, activeScene, selected, keyframeTime, keyframePos, startColor, endColor, height);

		}
	}

	private void drawKeyframe(PoseStack ms, PonderScene activeScene, boolean selected, int keyframeTime, int keyframePos, int startColor, int endColor, int height) {
		if (selected) {
			Font font = Minecraft.getInstance().font;
			GuiUtils.drawGradientRect(ms.last()
					.pose(), 100, keyframePos, 10, keyframePos + 1, 10 + height, endColor, startColor);
			ms.pushPose();
			ms.translate(0, 0, 100);
			String text;
			int offset;
			if (activeScene.currentTime < keyframeTime) {
				text = ">";
				offset = -1 - font.width(text);
			} else {
				text = "<";
				offset = 3;
			}
			font.draw(ms, text, keyframePos + offset, 10, endColor);
			ms.popPose();
		}

		GuiUtils.drawGradientRect(ms.last()
				.pose(), 500, keyframePos, -1, keyframePos + 1, 2 + height, startColor, endColor);
	}

	@Override
	public void playDownSound(SoundManager handler) {

	}
}
