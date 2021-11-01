package com.simibubi.create.foundation.ponder.ui;

import java.util.function.BiConsumer;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.Theme;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.gui.widgets.AbstractSimiWidget;
import com.simibubi.create.foundation.ponder.content.PonderChapter;

import net.minecraft.client.Minecraft;

public class ChapterLabel extends AbstractSimiWidget {

	private final PonderChapter chapter;
	private final PonderButton button;

	public ChapterLabel(PonderChapter chapter, int x, int y, BiConsumer<Integer, Integer> onClick) {
		super(x, y, 175, 38);

		this.button = new PonderButton(x + 4, y + 4, 30, 30)
				.showing(chapter)
				.withCallback(onClick);

		this.chapter = chapter;
	}

	@Override
	public void render(@Nonnull PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		UIRenderHelper.streak(ms, 0, x, y + height / 2, height - 2, width);
		Minecraft.getInstance().font.draw(ms, chapter.getTitle(), x + 50,
			y + 20, Theme.i(Theme.Key.TEXT_ACCENT_SLIGHT));

		button.renderButton(ms, mouseX, mouseY, partialTicks);
		super.render(ms, mouseX, mouseY, partialTicks);
	}

	@Override
	public void onClick(double x, double y) {
		if (!button.isMouseOver(x, y))
			return;

		button.runCallback(x, y);
	}
}
