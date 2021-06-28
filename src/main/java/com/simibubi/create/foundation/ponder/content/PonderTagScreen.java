package com.simibubi.create.foundation.ponder.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.gui.BoxElement;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.gui.Theme;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.ponder.NavigatableSimiScreen;
import com.simibubi.create.foundation.ponder.PonderLocalization;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import com.simibubi.create.foundation.ponder.PonderUI;
import com.simibubi.create.foundation.ponder.ui.ChapterLabel;
import com.simibubi.create.foundation.ponder.ui.LayoutHelper;
import com.simibubi.create.foundation.ponder.ui.PonderButton;
import com.simibubi.create.foundation.utility.FontHelper;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.Block;
import net.minecraft.client.MainWindow;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class PonderTagScreen extends NavigatableSimiScreen {

	public static final String ASSOCIATED = PonderLocalization.LANG_PREFIX + "associated";

	private final PonderTag tag;
	protected final List<Item> items;
	private final double itemXmult = 0.5;
	protected Rectangle2d itemArea;
	protected final List<PonderChapter> chapters;
	private final double chapterXmult = 0.5;
	private final double chapterYmult = 0.75;
	protected Rectangle2d chapterArea;
	private final double mainYmult = 0.15;

	private ItemStack hoveredItem = ItemStack.EMPTY;

	public PonderTagScreen(PonderTag tag) {
		this.tag = tag;
		items = new ArrayList<>();
		chapters = new ArrayList<>();
	}

	@Override
	protected void init() {
		widgets.clear();
		super.init();

		// items
		items.clear();
		PonderRegistry.tags.getItems(tag)
			.stream()
			.map(key -> {
				Item item = ForgeRegistries.ITEMS.getValue(key);
				if (item == null) {
					Block b = ForgeRegistries.BLOCKS.getValue(key);
					if (b != null)
						item = b.asItem();
				}
				return item;
			})
			.filter(Objects::nonNull)
			.forEach(items::add);

		if (!tag.getMainItem().isEmpty())
			items.remove(tag.getMainItem().getItem());

		int rowCount = MathHelper.clamp((int) Math.ceil(items.size() / 11d), 1, 3);
		LayoutHelper layout = LayoutHelper.centeredHorizontal(items.size(), rowCount, 28, 28, 8);
		itemArea = layout.getArea();
		int itemCenterX = (int) (width * itemXmult);
		int itemCenterY = getItemsY();

		for (Item i : items) {
			PonderButton b = new PonderButton(itemCenterX + layout.getX() + 4, itemCenterY + layout.getY() + 4)
					.showing(new ItemStack(i));

			if (PonderRegistry.all.containsKey(i.getRegistryName())) {
				b.withCallback((mouseX, mouseY) -> {
					centerScalingOn(mouseX, mouseY);
					ScreenOpener.transitionTo(PonderUI.of(new ItemStack(i), tag));
				});
			} else {
				if (i.getRegistryName()
						.getNamespace()
						.equals(Create.ID))
					b.withBorderColors(Theme.p(Theme.Key.PONDER_MISSING_CREATE))
							.animateColors(false);
				else
					b.withBorderColors(Theme.p(Theme.Key.PONDER_MISSING_VANILLA))
							.animateColors(false);
			}

			widgets.add(b);
			layout.next();
		}

		if (!tag.getMainItem().isEmpty()) {
			ResourceLocation registryName = tag.getMainItem()
					.getItem()
					.getRegistryName();

			PonderButton b = new PonderButton(itemCenterX - layout.getTotalWidth() / 2 - 42, itemCenterY - 10)
					.showing(tag.getMainItem());

			if (PonderRegistry.all.containsKey(registryName)) {
				b.withCallback((mouseX, mouseY) -> {
					centerScalingOn(mouseX, mouseY);
					ScreenOpener.transitionTo(PonderUI.of(tag.getMainItem(), tag));
				});
			} else {
				if (registryName.getNamespace()
						.equals(Create.ID))
					b.withBorderColors(Theme.p(Theme.Key.PONDER_MISSING_CREATE))
							.animateColors(false);
				else
					b.withBorderColors(Theme.p(Theme.Key.PONDER_MISSING_VANILLA))
							.animateColors(false);
			}

			widgets.add(b);
		}

		// chapters
		chapters.clear();
		chapters.addAll(PonderRegistry.tags.getChapters(tag));

		rowCount = MathHelper.clamp((int) Math.ceil(chapters.size() / 3f), 1, 3);
		layout = LayoutHelper.centeredHorizontal(chapters.size(), rowCount, 200, 38, 16);
		chapterArea = layout.getArea();
		int chapterCenterX = (int) (width * chapterXmult);
		int chapterCenterY = (int) (height * chapterYmult);

		for (PonderChapter chapter : chapters) {
			ChapterLabel label = new ChapterLabel(chapter, chapterCenterX + layout.getX(),
				chapterCenterY + layout.getY(), (mouseX, mouseY) -> {
					centerScalingOn(mouseX, mouseY);
					ScreenOpener.transitionTo(PonderUI.of(chapter));
				});

			widgets.add(label);
			layout.next();
		}

	}

	@Override
	public void tick() {
		super.tick();
		PonderUI.ponderTicks++;

		hoveredItem = ItemStack.EMPTY;
		MainWindow w = client.getWindow();
		double mouseX = client.mouseHelper.getMouseX() * w.getScaledWidth() / w.getWidth();
		double mouseY = client.mouseHelper.getMouseY() * w.getScaledHeight() / w.getHeight();
		for (Widget widget : widgets) {
			if (widget == backTrack)
				continue;
			if (widget instanceof PonderButton)
				if (widget.isMouseOver(mouseX, mouseY)) {
					hoveredItem = ((PonderButton) widget).getItem();
				}
		}
	}

	@Override
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		renderItems(ms, mouseX, mouseY, partialTicks);

		renderChapters(ms, mouseX, mouseY, partialTicks);

		ms.push();
		ms.translate(width / 2 - 120, height * mainYmult - 40, 0);

		ms.push();
		//ms.translate(0, 0, 800);
		int x = 31 + 20 + 8;
		int y = 31;

		String title = tag.getTitle();

		int streakHeight = 35;
		UIRenderHelper.streak(ms, 0, x - 4, y - 12 + streakHeight / 2, streakHeight, 240);
		//PonderUI.renderBox(ms, 21, 21, 30, 30, false);
		new BoxElement()
				.withBackground(Theme.c(Theme.Key.PONDER_BACKGROUND_FLAT))
				.gradientBorder(Theme.p(Theme.Key.PONDER_IDLE))
				.at(21, 21, 100)
				.withBounds(30, 30)
				.render(ms);

		textRenderer.draw(ms, Lang.translate(PonderUI.PONDERING), x, y - 6, Theme.i(Theme.Key.TEXT_DARKER));
		y += 8;
		x += 0;
		ms.translate(x, y, 0);
		ms.translate(0, 0, 5);
		textRenderer.draw(ms, title, 0, 0, Theme.i(Theme.Key.TEXT));
		ms.pop();

		ms.push();
		ms.translate(23, 23, 10);
		ms.scale(1.66f, 1.66f, 1.66f);
		tag.draw(ms, this, 0, 0);
		ms.pop();
		ms.pop();

		ms.push();
		int w = (int) (width * .45);
		x = (width - w) / 2;
		y = getItemsY() - 10 + Math.max(itemArea.getHeight(), 48);

		String desc = tag.getDescription();
		int h = textRenderer.getWordWrappedHeight(desc, w);


		//PonderUI.renderBox(ms, x - 3, y - 3, w + 6, h + 6, false);
		new BoxElement()
				.withBackground(Theme.c(Theme.Key.PONDER_BACKGROUND_FLAT))
				.gradientBorder(Theme.p(Theme.Key.PONDER_IDLE))
				.at(x - 3, y - 3, 90)
				.withBounds(w + 6, h + 6)
				.render(ms);

		ms.translate(0, 0, 100);
		FontHelper.drawSplitString(ms, textRenderer, desc, x, y, w, Theme.i(Theme.Key.TEXT));
		ms.pop();
	}

	protected void renderItems(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		if (items.isEmpty())
			return;

		int x = (int) (width * itemXmult);
		int y = getItemsY();

		String relatedTitle = Lang.translate(ASSOCIATED).getString();
		int stringWidth = textRenderer.getStringWidth(relatedTitle);

		ms.push();
		ms.translate(x, y, 0);
		//PonderUI.renderBox(ms, (sWidth - stringWidth) / 2 - 5, itemArea.getY() - 21, stringWidth + 10, 10, false);
		new BoxElement()
				.withBackground(Theme.c(Theme.Key.PONDER_BACKGROUND_FLAT))
				.gradientBorder(Theme.p(Theme.Key.PONDER_IDLE))
				.at((windowWidth - stringWidth) / 2f - 5, itemArea.getY() - 21, 100)
				.withBounds(stringWidth + 10, 10)
				.render(ms);

		ms.translate(0, 0, 200);

//		UIRenderHelper.streak(0, itemArea.getX() - 10, itemArea.getY() - 20, 20, 180, 0x101010);
		drawCenteredString(ms, textRenderer, relatedTitle, windowWidth / 2, itemArea.getY() - 20, Theme.i(Theme.Key.TEXT));

		ms.translate(0,0, -200);

		UIRenderHelper.streak(ms, 0, 0, 0, itemArea.getHeight() + 10, itemArea.getWidth() / 2 + 75);
		UIRenderHelper.streak(ms, 180, 0, 0, itemArea.getHeight() + 10, itemArea.getWidth() / 2 + 75);

		ms.pop();

	}

	public int getItemsY() {
		return (int) (mainYmult * height + 85);
	}

	protected void renderChapters(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		if (chapters.isEmpty())
			return;

		int chapterX = (int) (width * chapterXmult);
		int chapterY = (int) (height * chapterYmult);

		ms.push();
		ms.translate(chapterX, chapterY, 0);

		UIRenderHelper.streak(ms, 0, chapterArea.getX() - 10, chapterArea.getY() - 20, 20, 220);
		textRenderer.draw(ms, "More Topics to Ponder about", chapterArea.getX() - 5, chapterArea.getY() - 25, Theme.i(Theme.Key.TEXT_ACCENT_SLIGHT));

		ms.pop();
	}

	@Override
	protected void renderWindowForeground(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		ms.push();
		RenderSystem.disableRescaleNormal();
		RenderSystem.disableDepthTest();

		ms.translate(0, 0, 200);
		if (!hoveredItem.isEmpty()) {
			renderTooltip(ms, hoveredItem, mouseX, mouseY);
		}
		RenderSystem.enableDepthTest();
		RenderSystem.enableRescaleNormal();
		ms.pop();
	}

	@Override
	protected String getBreadcrumbTitle() {
		return tag.getTitle();
	}

	public ItemStack getHoveredTooltipItem() {
		return hoveredItem;
	}

	/*@Override
	public boolean mouseClicked(double x, double y, int button) {
		MutableBoolean handled = new MutableBoolean(false);
		widgets.forEach(w -> {
			if (handled.booleanValue())
				return;
			if (!w.isMouseOver(x, y))
				return;
			if (w instanceof PonderButton) {
				PonderButton mtdButton = (PonderButton) w;
				mtdButton.runCallback(x, y);
				handled.setTrue();
				return;
			}
		});

		if (handled.booleanValue())
			return true;
		return super.mouseClicked(x, y, button);
	}*/

	@Override
	public boolean isEquivalentTo(NavigatableSimiScreen other) {
		if (other instanceof PonderTagScreen)
			return tag == ((PonderTagScreen) other).tag;
		return super.isEquivalentTo(other);
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}

	public PonderTag getTag() {
		return tag;
	}
	
	@Override
	public void removed() {
		super.removed();
		hoveredItem = ItemStack.EMPTY;
	}

}
