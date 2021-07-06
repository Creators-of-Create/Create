package com.simibubi.create.foundation.ponder.content;

import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.BoxElement;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.gui.Theme;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.ponder.NavigatableSimiScreen;
import com.simibubi.create.foundation.ponder.PonderLocalization;
import com.simibubi.create.foundation.ponder.PonderUI;
import com.simibubi.create.foundation.ponder.ui.LayoutHelper;
import com.simibubi.create.foundation.ponder.ui.PonderButton;
import com.simibubi.create.foundation.utility.FontHelper;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.MainWindow;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class PonderTagIndexScreen extends NavigatableSimiScreen {

	public static final String EXIT = PonderLocalization.LANG_PREFIX + "exit";
	public static final String TITLE = PonderLocalization.LANG_PREFIX + "index_title";
	public static final String WELCOME = PonderLocalization.LANG_PREFIX + "welcome";
	public static final String CATEGORIES = PonderLocalization.LANG_PREFIX + "categories";
	public static final String DESCRIPTION = PonderLocalization.LANG_PREFIX + "index_description";

	private final double itemXmult = 0.5;
	protected Rectangle2d itemArea;
	protected Rectangle2d chapterArea;
	private final double mainYmult = 0.15;

	private PonderTag hoveredItem = null;

	// The ponder entry point from the menu. May be changed to include general
	// chapters in the future
	public PonderTagIndexScreen() {}

	@Override
	protected void init() {
		widgets.clear();
		super.init();

		List<PonderTag> tags = PonderTag.LISTED_TAGS;
		int rowCount = MathHelper.clamp((int) Math.ceil(tags.size() / 11d), 1, 3);
		LayoutHelper layout = LayoutHelper.centeredHorizontal(tags.size(), rowCount, 28, 28, 8);
		itemArea = layout.getArea();
		int itemCenterX = (int) (width * itemXmult);
		int itemCenterY = getItemsY();

		for (PonderTag i : tags) {
			PonderButton b =
				new PonderButton(itemCenterX + layout.getX() + 4, itemCenterY + layout.getY() + 4).showingTag(i)
					.withCallback((mouseX, mouseY) -> {
						centerScalingOn(mouseX, mouseY);
						ScreenOpener.transitionTo(new PonderTagScreen(i));
					});
			widgets.add(b);
			layout.next();
		}

		widgets.add(backTrack = new PonderButton(31, height - 31 - 20).enableFade(0, 5)
			.showing(AllIcons.I_MTD_CLOSE)
			.withCallback(() -> ScreenOpener.openPreviousScreen(this, Optional.empty())));
		backTrack.fade(1);
	}

	@Override
	public void tick() {
		super.tick();
		PonderUI.ponderTicks++;

		hoveredItem = null;
		MainWindow w = client.getWindow();
		double mouseX = client.mouseHelper.getMouseX() * w.getScaledWidth() / w.getWidth();
		double mouseY = client.mouseHelper.getMouseY() * w.getScaledHeight() / w.getHeight();
		for (Widget widget : widgets) {
			if (widget == backTrack)
				continue;
			if (widget instanceof PonderButton)
				if (widget.isMouseOver(mouseX, mouseY))
					hoveredItem = ((PonderButton) widget).getTag();
		}
	}
	
	@Override
	protected String backTrackingLangKey() {
		return EXIT;
	}

	@Override
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		renderItems(ms, mouseX, mouseY, partialTicks);

		ms.push();
		ms.translate(width / 2 - 120, height * mainYmult - 40, 0);

		ms.push();
		// ms.translate(0, 0, 800);
		int x = 31 + 20 + 8;
		int y = 31;

		String title = Lang.translate(WELCOME)
			.getString();

		int streakHeight = 35;
		UIRenderHelper.streak(ms, 0, x - 4, y - 12 + streakHeight / 2, streakHeight, 240);
		// PonderUI.renderBox(ms, 21, 21, 30, 30, false);
		new BoxElement().withBackground(Theme.c(Theme.Key.PONDER_BACKGROUND_FLAT))
			.gradientBorder(Theme.p(Theme.Key.PONDER_IDLE))
			.at(21, 21, 100)
			.withBounds(30, 30)
			.render(ms);

		textRenderer.draw(ms, title, x + 8, y + 1, Theme.i(Theme.Key.TEXT));
//		y += 8;
//		x += 0;
//		ms.translate(x, y, 0);
//		ms.translate(0, 0, 5);
//		textRenderer.draw(ms, title, 0, 0, Theme.i(Theme.Key.TEXT));
		ms.pop();

		ms.push();
		ms.translate(23, 23, 10);
		ms.scale(1.66f, 1.66f, 1.66f);
		ms.translate(-4, -4, 0);
		ms.scale(1.5f, 1.5f, 1.5f);
		GuiGameElement.of(AllItems.WRENCH.asStack())
			.render(ms);
		ms.pop();
		ms.pop();

		ms.push();
		int w = (int) (width * .45);
		x = (width - w) / 2;
		y = getItemsY() - 10 + Math.max(itemArea.getHeight(), 48);

		String desc = Lang.translate(DESCRIPTION)
			.getString();
		int h = textRenderer.getWordWrappedHeight(desc, w);

		// PonderUI.renderBox(ms, x - 3, y - 3, w + 6, h + 6, false);
		new BoxElement().withBackground(Theme.c(Theme.Key.PONDER_BACKGROUND_FLAT))
			.gradientBorder(Theme.p(Theme.Key.PONDER_IDLE))
			.at(x - 3, y - 3, 90)
			.withBounds(w + 6, h + 6)
			.render(ms);

		ms.translate(0, 0, 100);
		FontHelper.drawSplitString(ms, textRenderer, desc, x, y, w, Theme.i(Theme.Key.TEXT));
		ms.pop();
	}

	protected void renderItems(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		List<PonderTag> tags = PonderTag.LISTED_TAGS;
		if (tags.isEmpty())
			return;

		int x = (int) (width * itemXmult);
		int y = getItemsY();

		String relatedTitle = Lang.translate(CATEGORIES)
			.getString();
		int stringWidth = textRenderer.getStringWidth(relatedTitle);

		ms.push();
		ms.translate(x, y, 0);
		// PonderUI.renderBox(ms, (sWidth - stringWidth) / 2 - 5, itemArea.getY() - 21,
		// stringWidth + 10, 10, false);
		new BoxElement().withBackground(Theme.c(Theme.Key.PONDER_BACKGROUND_FLAT))
			.gradientBorder(Theme.p(Theme.Key.PONDER_IDLE))
			.at((windowWidth - stringWidth) / 2f - 5, itemArea.getY() - 21, 100)
			.withBounds(stringWidth + 10, 10)
			.render(ms);

		ms.translate(0, 0, 200);

//		UIRenderHelper.streak(0, itemArea.getX() - 10, itemArea.getY() - 20, 20, 180, 0x101010);
		drawCenteredString(ms, textRenderer, relatedTitle, windowWidth / 2, itemArea.getY() - 20, Theme.i(Theme.Key.TEXT));

		ms.translate(0, 0, -200);

		UIRenderHelper.streak(ms, 0, 0, 0, itemArea.getHeight() + 10, itemArea.getWidth() / 2 + 75);
		UIRenderHelper.streak(ms, 180, 0, 0, itemArea.getHeight() + 10, itemArea.getWidth() / 2 + 75);

		ms.pop();

	}

	public int getItemsY() {
		return (int) (mainYmult * height + 85);
	}

	@Override
	protected void renderWindowForeground(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		ms.push();
		RenderSystem.disableRescaleNormal();
		RenderSystem.disableDepthTest();

		ms.translate(0, 0, 200);
		if (hoveredItem != null) {
			List<ITextComponent> list = TooltipHelper.cutStringTextComponent(hoveredItem.getDescription(),
				TextFormatting.GRAY, TextFormatting.GRAY);
			list.add(0, new StringTextComponent(hoveredItem.getTitle()));
			renderTooltip(ms, list, mouseX, mouseY);
		}
		RenderSystem.enableDepthTest();
		RenderSystem.enableRescaleNormal();
		ms.pop();
	}

	@Override
	protected String getBreadcrumbTitle() {
		return Lang.translate(TITLE)
			.getString();
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}

	@Override
	public void removed() {
		super.removed();
		hoveredItem = null;
	}

}
