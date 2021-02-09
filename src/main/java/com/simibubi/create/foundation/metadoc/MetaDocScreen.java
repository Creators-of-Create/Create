package com.simibubi.create.foundation.metadoc;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.LerpedFloat;
import com.simibubi.create.foundation.utility.LerpedFloat.Chaser;
import com.simibubi.create.foundation.utility.MatrixStacker;

import net.minecraftforge.fml.client.gui.GuiUtils;

public class MetaDocScreen extends AbstractSimiScreen {

	private List<MetaDocScene> stories;
	private int index = 0;
	private LerpedFloat fadeIn;

	public MetaDocScreen(List<MetaDocScene> stories) {
		this.stories = stories;
		fadeIn = LerpedFloat.linear()
			.startWithValue(0)
			.chase(1, .5f, Chaser.EXP);
	}

	@Override
	public void tick() {
		fadeIn.tickChaser();
		stories.get(index)
			.tick();
	}

	@Override
	protected void renderWindow(int mouseX, int mouseY, float partialTicks) {
		RenderSystem.enableBlend();
		renderStory();
		renderWidgets(partialTicks);
	}

	protected void renderStory() {
		MetaDocScene story = stories.get(index);
		MatrixStack ms = new MatrixStack();
		ms.push();

		ms.translate(width / 2, height / 2, 200);
		MatrixStacker.of(ms)
			.rotateX(-45)
			.rotateY(45);
		ms.scale(30, -30, 30);

		SuperRenderTypeBuffer buffer = SuperRenderTypeBuffer.getInstance();
		story.render(buffer, ms);
		buffer.draw();
		ms.pop();
	}

	protected void renderWidgets(float pt) {
		float fade = fadeIn.getValue(pt);
		int textColor = 0xeeeeee;

		drawString(font, "MetaDoc Experimental 0", 50, 50 - 16, textColor);

		RenderSystem.pushMatrix();

		if (fade < 1)
			RenderSystem.translated(0, (1 - fade) * 5, 0);

		int closeWidth = 24;
		int closeHeight = 24;
		int closeX = (width - closeWidth) / 2;
		int closeY = height - closeHeight - 31;
		renderBox(closeX, closeY, closeWidth, closeHeight, 0xdd000000, 0x30eebb00, 0x10eebb00);
		AllIcons.I_CONFIRM.draw(closeX + 4, closeY + 4);

		RenderSystem.popMatrix();
	}

	protected void renderBox(int tooltipX, int tooltipY, int tooltipTextWidth, int tooltipHeight, int backgroundColor,
		int borderColorStart, int borderColorEnd) {
		int zLevel = 400;
		GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3, tooltipY - 3,
			backgroundColor, backgroundColor);
		GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipTextWidth + 3,
			tooltipY + tooltipHeight + 4, backgroundColor, backgroundColor);
		GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3,
			tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
		GuiUtils.drawGradientRect(zLevel, tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3,
			backgroundColor, backgroundColor);
		GuiUtils.drawGradientRect(zLevel, tooltipX + tooltipTextWidth + 3, tooltipY - 3,
			tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
		GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1,
			tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
		GuiUtils.drawGradientRect(zLevel, tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1,
			tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
		GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY - 3 + 1,
			borderColorStart, borderColorStart);
		GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipTextWidth + 3,
			tooltipY + tooltipHeight + 3, borderColorEnd, borderColorEnd);
	}

}
