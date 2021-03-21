package com.simibubi.create.foundation.ponder.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.gui.ScreenOpener;
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

		int rowCount = MathHelper.clamp((int) Math.ceil(items.size() / 11d), 1, 3);
		LayoutHelper layout = LayoutHelper.centeredHorizontal(items.size(), rowCount, 28, 28, 8);
		itemArea = layout.getArea();
		int itemCenterX = (int) (width * itemXmult);
		int itemCenterY = getItemsY();

		for (Item i : items) {
			final boolean canClick = PonderRegistry.all.containsKey(i.getRegistryName());
			PonderButton button =
				new PonderButton(itemCenterX + layout.getX() + 4, itemCenterY + layout.getY() + 4, (mouseX, mouseY) -> {
					if (!canClick)
						return;
					centerScalingOn(mouseX, mouseY);
					ScreenOpener.transitionTo(PonderUI.of(new ItemStack(i), tag));
				}).showing(new ItemStack(i));
			if (!canClick)
				if (i.getRegistryName()
					.getNamespace()
					.equals(Create.ID))
					button.customColors(0x70984500, 0x70692400);
				else
					button.customColors(0x505000FF, 0x50300077);

			button.fade(1);
			widgets.add(button);
			layout.next();
		}

		if (!tag.getMainItem()
			.isEmpty()) {
			ResourceLocation registryName = tag.getMainItem()
				.getItem()
				.getRegistryName();
			final boolean canClick = PonderRegistry.all.containsKey(registryName);
			PonderButton button =
				new PonderButton(itemCenterX - layout.getTotalWidth() / 2 - 42, itemCenterY - 10, (mouseX, mouseY) -> {
					if (!canClick)
						return;
					centerScalingOn(mouseX, mouseY);
					ScreenOpener.transitionTo(PonderUI.of(tag.getMainItem(), tag));
				}).showing(tag.getMainItem());
			if (!canClick)
				if (registryName.getNamespace()
					.equals(Create.ID))
					button.customColors(0x70984500, 0x70692400);
				else
					button.customColors(0x505000FF, 0x50300077);

			button.fade(1);
			widgets.add(button);
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
		MainWindow w = minecraft.getWindow();
		double mouseX = minecraft.mouseHelper.getMouseX() * w.getScaledWidth() / w.getWidth();
		double mouseY = minecraft.mouseHelper.getMouseY() * w.getScaledHeight() / w.getHeight();
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
	protected void renderWindow(int mouseX, int mouseY, float partialTicks) {
		renderItems(mouseX, mouseY, partialTicks);

		renderChapters(mouseX, mouseY, partialTicks);

		RenderSystem.pushMatrix();
		RenderSystem.translated(width / 2 - 120, height * mainYmult - 40, 0);

		RenderSystem.pushMatrix();
		RenderSystem.translated(0, 0, 800);
		int x = 31 + 20 + 8;
		int y = 31;

		String title = tag.getTitle();

		int streakHeight = 35;
		UIRenderHelper.streak(0, x - 4, y - 12 + streakHeight / 2, streakHeight, (int) (240), 0x101010);
		PonderUI.renderBox(21, 21, 30, 30, false);

		drawString(font, Lang.translate(PonderUI.PONDERING), x, y - 6, 0xffa3a3a3);
		y += 8;
		x += 0;
		RenderSystem.translated(x, y, 0);
		RenderSystem.translated(0, 0, 5);
		font.drawString(title, 0, 0, 0xeeeeee);
		RenderSystem.popMatrix();

		RenderSystem.pushMatrix();
		RenderSystem.translated(23, 23, 0);
		RenderSystem.scaled(1.66, 1.66, 1.66);
		tag.draw(this, 0, 0);
		RenderSystem.popMatrix();
		RenderSystem.popMatrix();

		RenderSystem.pushMatrix();
		int w = (int) (width * .45);
		x = (width - w) / 2;
		y = getItemsY() - 10 + Math.max(itemArea.getHeight(), 48);

		String desc = tag.getDescription();
		int h = font.getWordWrappedHeight(desc, w);

		PonderUI.renderBox(x - 3, y - 3, w + 6, h + 6, false);
		RenderSystem.translated(0, 0, 100);
		FontHelper.drawSplitString(font, desc, x, y, w, 0xeeeeee);
		RenderSystem.popMatrix();

	}

	protected void renderItems(int mouseX, int mouseY, float partialTicks) {
		if (items.isEmpty())
			return;

		int x = (int) (width * itemXmult);
		int y = getItemsY();

		String relatedTitle = Lang.translate(ASSOCIATED);
		int stringWidth = font.getStringWidth(relatedTitle);

		RenderSystem.pushMatrix();
		RenderSystem.translated(x, y, 0);
		PonderUI.renderBox((sWidth - stringWidth) / 2 - 5, itemArea.getY() - 21, stringWidth + 10, 10, false);
		RenderSystem.translated(0, 0, 200);

//		UIRenderHelper.streak(0, itemArea.getX() - 10, itemArea.getY() - 20, 20, 180, 0x101010);
		drawCenteredString(font, relatedTitle, sWidth / 2, itemArea.getY() - 20, 0xeeeeee);

		UIRenderHelper.streak(0, 0, 0, itemArea.getHeight() + 10, itemArea.getWidth() / 2 + 75, 0x101010);
		UIRenderHelper.streak(180, 0, 0, itemArea.getHeight() + 10, itemArea.getWidth() / 2 + 75, 0x101010);

		RenderSystem.popMatrix();

	}

	public int getItemsY() {
		return (int) (mainYmult * height + 85);
	}

	protected void renderChapters(int mouseX, int mouseY, float partialTicks) {
		if (chapters.isEmpty())
			return;

		int chapterX = (int) (width * chapterXmult);
		int chapterY = (int) (height * chapterYmult);

		RenderSystem.pushMatrix();
		RenderSystem.translated(chapterX, chapterY, 0);

		UIRenderHelper.streak(0, chapterArea.getX() - 10, chapterArea.getY() - 20, 20, 220, 0x101010);
		drawString(font, "More Topics to Ponder about", chapterArea.getX() - 5, chapterArea.getY() - 25, 0xffddeeff);

		RenderSystem.popMatrix();
	}

	@Override
	protected void renderWindowForeground(int mouseX, int mouseY, float partialTicks) {
		RenderSystem.pushMatrix();
		RenderSystem.disableRescaleNormal();
		RenderSystem.disableDepthTest();

		RenderSystem.translated(0, 0, 200);
		if (!hoveredItem.isEmpty()) {
			renderTooltip(hoveredItem, mouseX, mouseY);
		}
		RenderSystem.enableDepthTest();
		RenderSystem.enableRescaleNormal();
		RenderSystem.popMatrix();
	}

	@Override
	protected String getBreadcrumbTitle() {
		return tag.getTitle();
	}

	public ItemStack getHoveredTooltipItem() {
		return hoveredItem;
	}

	@Override
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
	}

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

}
