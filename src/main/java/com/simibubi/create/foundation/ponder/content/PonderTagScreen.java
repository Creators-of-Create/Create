package com.simibubi.create.foundation.ponder.content;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import com.simibubi.create.foundation.ponder.PonderUI;
import com.simibubi.create.foundation.ponder.ui.ChapterLabel;
import com.simibubi.create.foundation.ponder.ui.LayoutHelper;
import com.simibubi.create.foundation.ponder.ui.PonderButton;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.block.Block;
import net.minecraft.client.MainWindow;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PonderTagScreen extends AbstractSimiScreen {

	protected final PonderTag tag;
	protected final List<Item> items;
	private final double itemXmult = 0.5;
	private final double itemYmult = 0.4;
	protected Rectangle2d itemArea;
	protected final List<PonderChapter> chapters;
	private final double chapterXmult = 0.5;
	private final double chapterYmult = 0.75;
	protected Rectangle2d chapterArea;
	private final double mainXmult = 0.5;
	private final double mainYmult = 0.15;

	private ItemStack hoveredItem = ItemStack.EMPTY;


	public PonderTagScreen(PonderTag tag) {
		this.tag = tag;
		items = new ArrayList<>();
		chapters = new ArrayList<>();
	}

	@Override
	protected void init() {
		super.init();
		widgets.clear();

		//items
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
		int itemCenterY = (int) (height * itemYmult);

		for (Item i : items) {
			PonderButton button = new PonderButton(itemCenterX + layout.getX() + 4, itemCenterY + layout.getY() + 4, () -> {})
					.showing(new ItemStack(i));

			button.fade(1);
			widgets.add(button);
			layout.next();
		}

		//chapters
		chapters.clear();
		chapters.addAll(PonderRegistry.tags.getChapters(tag));

		rowCount = MathHelper.clamp((int) Math.ceil(chapters.size() / 3f), 1, 3);
		layout = LayoutHelper.centeredHorizontal(chapters.size(), rowCount, 200, 38, 16);
		chapterArea = layout.getArea();
		int chapterCenterX = (int) (width * chapterXmult);
		int chapterCenterY = (int) (height * chapterYmult);

		for (PonderChapter chapter : chapters) {
			ChapterLabel label = new ChapterLabel(chapter, chapterCenterX + layout.getX(), chapterCenterY + layout.getY(), () -> {
				ScreenOpener.transitionTo(PonderUI.of(chapter));
			});

			widgets.add(label);
			layout.next();
		}

	}

	@Override
	public void tick() {
		super.tick();

		hoveredItem = ItemStack.EMPTY;
		MainWindow w = minecraft.getWindow();
		double mouseX = minecraft.mouseHelper.getMouseX() * w.getScaledWidth() / w.getWidth();
		double mouseY = minecraft.mouseHelper.getMouseY() * w.getScaledHeight() / w.getHeight();
		for (Widget widget : widgets) {
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

		//
		int x = (int) (width * mainXmult);
		int y = (int) (height * mainYmult);

		RenderSystem.pushMatrix();
		RenderSystem.translated(x, y, 0);
		RenderSystem.translated(-150, 0, 0);

		if (!tag.getMainItem().isEmpty()) {
			RenderSystem.translated(-25, 0, 0);
			PonderUI.renderBox(0, -10, 20, 20, false);
			RenderSystem.pushMatrix();
			RenderSystem.translated(-2, -12, 0);
			RenderSystem.scaled(1.5, 1.5, 1);
			GuiGameElement.of(tag.getMainItem()).render();

			RenderSystem.popMatrix();

			RenderSystem.translated(75, 0, 0);

		}

		RenderSystem.pushMatrix();
		RenderSystem.scaled(1.5, 1.5, 1);


		//render icon & box
		PonderUI.renderBox(0, -10, 20, 20, true);
		RenderSystem.translated(2, 2 - 10, 100);
		tag.draw(this, 0, 0);

		RenderSystem.popMatrix();

		//tag name & description
		UIRenderHelper.streak(0, 36, 0, 39, 350, 0x101010);
		drawString(font, Lang.translate("ponder.tag." + tag.getId()), 41, -16, 0xffff_ffff);
		drawString(font, Lang.translate("ponder.tag." + tag.getId() + ".desc"), 41, -4, 0xffff_ffff);

		RenderSystem.popMatrix();

	}

	protected void renderItems(int mouseX, int mouseY, float partialTicks) {
		if (items.isEmpty())
			return;

		int x = (int) (width * itemXmult);
		int y = (int) (height * itemYmult);

		RenderSystem.pushMatrix();
		RenderSystem.translated(x, y, 0);

		UIRenderHelper.streak(0, itemArea.getX() - 10, itemArea.getY() - 20, 20, 180, 0x101010);
		drawString(font, "Related Items", itemArea.getX() - 5, itemArea.getY() - 25, 0xffddeeff);

		UIRenderHelper.streak(0, 0, 0, itemArea.getHeight() + 10, itemArea.getWidth()/2 + 75, 0x101010);
		UIRenderHelper.streak(180, 0, 0, itemArea.getHeight() + 10, itemArea.getWidth()/2 + 75, 0x101010);

		RenderSystem.popMatrix();

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
		return Lang.translate("ponder.tag." + tag.getId());
	}

	public ItemStack getHoveredTooltipItem() {
		return hoveredItem;
	}

}
