package com.simibubi.create.foundation.ponder.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.matrix.MatrixStack;
import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.crank.ValveHandleBlock;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.ponder.NavigatableSimiScreen;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import com.simibubi.create.foundation.ponder.PonderUI;
import com.simibubi.create.foundation.ponder.ui.ChapterLabel;
import com.simibubi.create.foundation.ponder.ui.LayoutHelper;
import com.simibubi.create.foundation.ponder.ui.PonderButton;

import net.minecraft.block.Block;
import net.minecraft.client.MainWindow;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class PonderIndexScreen extends NavigatableSimiScreen {

	protected final List<PonderChapter> chapters;
	private final double chapterXmult = 0.5;
	private final double chapterYmult = 0.3;
	protected Rectangle2d chapterArea;

	protected final List<Item> items;
	private final double itemXmult = 0.5;
	private double itemYmult = 0.75;
	protected Rectangle2d itemArea;

	private ItemStack hoveredItem = ItemStack.EMPTY;

	public PonderIndexScreen() {
		chapters = new ArrayList<>();
		items = new ArrayList<>();
	}

	@Override
	protected void init() {
		widgets.clear();
		super.init();

		chapters.clear();
		// chapters.addAll(PonderRegistry.chapters.getAllChapters());

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
			.filter(PonderIndexScreen::exclusions)
			.forEach(items::add);

		boolean hasChapters = !chapters.isEmpty();

		// setup chapters
		LayoutHelper layout = LayoutHelper.centeredHorizontal(chapters.size(),
			MathHelper.clamp((int) Math.ceil(chapters.size() / 4f), 1, 4), 200, 38, 16);
		chapterArea = layout.getArea();
		int chapterCenterX = (int) (width * chapterXmult);
		int chapterCenterY = (int) (height * chapterYmult);

		// todo at some point pagination or horizontal scrolling may be needed for
		// chapters/items
		for (PonderChapter chapter : chapters) {
			ChapterLabel label = new ChapterLabel(chapter, chapterCenterX + layout.getX(),
				chapterCenterY + layout.getY(), (mouseX, mouseY) -> {
					centerScalingOn(mouseX, mouseY);
					ScreenOpener.transitionTo(PonderUI.of(chapter));
				});

			widgets.add(label);
			layout.next();
		}

		// setup items
		if (!hasChapters) {
			itemYmult = 0.5;
		}

		int maxItemRows = hasChapters ? 4 : 7;
		layout = LayoutHelper.centeredHorizontal(items.size(),
			MathHelper.clamp((int) Math.ceil(items.size() / 11f), 1, maxItemRows), 28, 28, 8);
		itemArea = layout.getArea();
		int itemCenterX = (int) (width * itemXmult);
		int itemCenterY = (int) (height * itemYmult);

		for (Item item : items) {
			PonderButton button =
				new PonderButton(itemCenterX + layout.getX() + 4, itemCenterY + layout.getY() + 4, (x, y) -> {
					if (!PonderRegistry.all.containsKey(item.getRegistryName()))
						return;

					centerScalingOn(x, y);
					ScreenOpener.transitionTo(PonderUI.of(new ItemStack(item)));
				}).showing(new ItemStack(item));

			button.fade(1);
			widgets.add(button);
			layout.next();
		}

	}

	private static boolean exclusions(Item item) {
		if (item instanceof BlockItem) {
			Block block = ((BlockItem) item).getBlock();
			if (block instanceof ValveHandleBlock && !AllBlocks.COPPER_VALVE_HANDLE.is(item))
				return false;
		}

		return true;
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
			if (widget instanceof PonderButton)
				if (widget.isMouseOver(mouseX, mouseY)) {
					hoveredItem = ((PonderButton) widget).getItem();
				}
		}
	}

	@Override
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		int x = (int) (width * chapterXmult);
		int y = (int) (height * chapterYmult);

		if (!chapters.isEmpty()) {
			ms.push();
			ms.translate(x, y, 0);

			UIRenderHelper.streak(ms, 0, chapterArea.getX() - 10, chapterArea.getY() - 20, 20, 220, 0x101010);
			textRenderer.draw(ms, "Topics to Ponder about", chapterArea.getX() - 5, chapterArea.getY() - 25, 0xffddeeff);

			ms.pop();
		}

		x = (int) (width * itemXmult);
		y = (int) (height * itemYmult);

		ms.push();
		ms.translate(x, y, 0);

		UIRenderHelper.streak(ms, 0, itemArea.getX() - 10, itemArea.getY() - 20, 20, 220, 0x101010);
		textRenderer.draw(ms, "Items to inspect", itemArea.getX() - 5, itemArea.getY() - 25, 0xffddeeff);

		ms.pop();
	}

	@Override
	protected void renderWindowForeground(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		if (hoveredItem.isEmpty())
			return;

		ms.push();
		ms.translate(0, 0, 200);

		renderTooltip(ms, hoveredItem, mouseX, mouseY);

		ms.pop();
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
				PonderButton btn = (PonderButton) w;
				btn.runCallback(x, y);
				handled.setTrue();
			}
		});

		if (handled.booleanValue())
			return true;
		return super.mouseClicked(x, y, button);
	}

	@Override
	public boolean isEquivalentTo(NavigatableSimiScreen other) {
		return other instanceof PonderIndexScreen;
	}

	public ItemStack getHoveredTooltipItem() {
		return hoveredItem;
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}
}
