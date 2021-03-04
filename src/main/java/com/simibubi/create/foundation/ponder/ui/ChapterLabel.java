package com.simibubi.create.foundation.ponder.ui;

import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.gui.widgets.AbstractSimiWidget;
import com.simibubi.create.foundation.ponder.content.PonderChapter;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.client.Minecraft;

public class ChapterLabel extends AbstractSimiWidget {

	private final PonderChapter chapter;
	private final PonderButton button;

	public ChapterLabel(PonderChapter chapter, int x, int y, Runnable onClick) {
		super(x, y, 175, 38);

		this.button = new PonderButton(x + 4, y + 4, onClick, 30, 30).showing(chapter);
		this.button.fade(1);

		this.chapter = chapter;
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		UIRenderHelper.streak(0, x, y + height/2, height - 2, width, 0x101010);
		drawString(Minecraft.getInstance().fontRenderer, Lang.translate("ponder.chapter." + chapter.getId()), x + 50, y + 20, 0xffddeeff);

		button.renderButton(mouseX, mouseY, partialTicks);
		super.render(mouseX, mouseY, partialTicks);
	}

	@Override
	public void onClick(double x, double y) {
		if (!button.isMouseOver(x, y))
			return;

		button.runCallback();
	}
}
