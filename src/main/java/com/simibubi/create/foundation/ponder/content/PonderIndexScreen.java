package com.simibubi.create.foundation.ponder.content;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import com.simibubi.create.foundation.ponder.PonderUI;
import com.simibubi.create.foundation.ponder.ui.ChapterLabel;
import com.simibubi.create.foundation.ponder.ui.LayoutHelper;
import com.simibubi.create.foundation.ponder.ui.PonderButton;
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

public class PonderIndexScreen extends AbstractSimiScreen {

	protected final List<PonderChapter> chapters;
	private final double chapterXmult = 0.5;
	private final double chapterYmult = 0.3;
	protected Rectangle2d chapterArea;

	protected final List<Item> items;
	private final double itemXmult = 0.5;
	private final double itemYmult = 0.75;
	protected Rectangle2d itemArea;

	private ItemStack hoveredItem = ItemStack.EMPTY;

	public PonderIndexScreen() {
		chapters = new ArrayList<>();
		items = new ArrayList<>();
	}

	@Override
	protected void init() {
		super.init();

		widgets.clear();

		chapters.clear();
		chapters.addAll(PonderRegistry.chapters.getAllChapters());

		LayoutHelper layout = LayoutHelper.centeredHorizontal(
				chapters.size(),
				MathHelper.clamp((int) Math.ceil(chapters.size() / 4f), 1, 4),
				200,
				38,
				16
		);
		chapterArea = layout.getArea();
		int chapterCenterX = (int) (width * chapterXmult);
		int chapterCenterY = (int) (height * chapterYmult);

		//todo at some point pagination or horizontal scrolling may be needed for chapters/items
		for (PonderChapter chapter : chapters) {
			ChapterLabel label = new ChapterLabel(chapter, chapterCenterX + layout.getX(), chapterCenterY + layout.getY(), () -> {
				ScreenOpener.transitionTo(PonderUI.of(chapter));
			});

			widgets.add(label);
			layout.next();
		}

		items.clear();
		PonderRegistry.all.keySet()
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

		layout = LayoutHelper.centeredHorizontal(
				items.size(),
				MathHelper.clamp((int) Math.ceil(items.size() / 11f), 1, 4),
				28,
				28,
				8
		);
		itemArea = layout.getArea();
		int itemCenterX = (int) (width * itemXmult);
		int itemCenterY = (int) (height * itemYmult);

		for (Item item : items) {
			PonderButton button = new PonderButton(itemCenterX + layout.getX() + 4, itemCenterY + layout.getY() + 4, () -> {})
					.showing(new ItemStack(item));

			button.fade(1);
			widgets.add(button);
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
		int x = (int) (width * chapterXmult);
		int y = (int) (height * chapterYmult);

		RenderSystem.pushMatrix();
		RenderSystem.translated(x, y, 0);

		UIRenderHelper.streak(0, chapterArea.getX() - 10, chapterArea.getY() - 20, 20, 220, 0x101010);
		drawString(font, "Topics to Ponder about", chapterArea.getX() - 5, chapterArea.getY() - 25, 0xffddeeff);

		RenderSystem.popMatrix();


		x = (int) (width * itemXmult);
		y = (int) (height * itemYmult);

		RenderSystem.pushMatrix();
		RenderSystem.translated(x, y, 0);

		UIRenderHelper.streak(0, itemArea.getX() - 10, itemArea.getY() - 20, 20, 220, 0x101010);
		drawString(font, "Items to inspect", itemArea.getX() - 5, itemArea.getY() - 25, 0xffddeeff);

		RenderSystem.popMatrix();
	}

	@Override
	protected void renderWindowForeground(int mouseX, int mouseY, float partialTicks) {
		if (hoveredItem.isEmpty())
			return;

		RenderSystem.pushMatrix();
		RenderSystem.translated(0, 0, 200);

		renderTooltip(hoveredItem, mouseX, mouseY);

		RenderSystem.popMatrix();
	}

	public ItemStack getHoveredTooltipItem() {
		return hoveredItem;
	}
}
