package com.simibubi.create.foundation.metadoc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.block.Blocks;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class MetaDocScreen extends AbstractSimiScreen {

	@Override
	protected void renderWindow(int mouseX, int mouseY, float partialTicks) {

		int tooltipX = 50;
		int tooltipY = 50;
		int tooltipTextWidth = width - 100;
		int backgroundColor = GuiUtils.DEFAULT_BACKGROUND_COLOR;
		int borderColorStart = GuiUtils.DEFAULT_BORDER_COLOR_START;
		int borderColorEnd = GuiUtils.DEFAULT_BORDER_COLOR_END;
		int zLevel = 100;
		int tooltipHeight = height - 100;

		drawString(font, "MetaDoc Experimental 0", tooltipX, tooltipY - 16, 0xffffff);

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

		RenderSystem.pushMatrix();
		RenderSystem.translated(width/2, height/2, 200);
		GuiGameElement.of(Blocks.DIAMOND_BLOCK.getDefaultState())
			.rotate(22.5, AnimationTickHolder.getRenderTick() % 360f, 0)
			.scale(50)
			.render();
		RenderSystem.popMatrix();

	}

}
