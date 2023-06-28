package com.simibubi.create.foundation.ponder.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.crank.ValveHandleBlock;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.gui.Theme;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.ponder.PonderChapter;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import com.simibubi.create.foundation.utility.RegisteredObjects;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class PonderIndexScreen extends NavigatableSimiScreen {

	protected final List<PonderChapter> chapters;
	private final double chapterXmult = 0.5;
	private final double chapterYmult = 0.3;
	protected Rect2i chapterArea;

	protected final List<Item> items;
	private final double itemXmult = 0.5;
	private double itemYmult = 0.75;
	protected Rect2i itemArea;

	private ItemStack hoveredItem = ItemStack.EMPTY;

	public PonderIndexScreen() {
		chapters = new ArrayList<>();
		items = new ArrayList<>();
	}

	@Override
	protected void init() {
		super.init();

		chapters.clear();
		// chapters.addAll(PonderRegistry.CHAPTERS.getAllChapters());

		items.clear();
		PonderRegistry.ALL.keySet()
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
			Mth.clamp((int) Math.ceil(chapters.size() / 4f), 1, 4), 200, 38, 16);
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

			addRenderableWidget(label);
			layout.next();
		}

		// setup items
		if (!hasChapters) {
			itemYmult = 0.5;
		}

		int maxItemRows = hasChapters ? 4 : 7;
		layout = LayoutHelper.centeredHorizontal(items.size(),
			Mth.clamp((int) Math.ceil(items.size() / 11f), 1, maxItemRows), 28, 28, 8);
		itemArea = layout.getArea();
		int itemCenterX = (int) (width * itemXmult);
		int itemCenterY = (int) (height * itemYmult);

		for (Item item : items) {
			PonderButton b = new PonderButton(itemCenterX + layout.getX() + 4, itemCenterY + layout.getY() + 4)
					.showing(new ItemStack(item))
					.withCallback((x, y) -> {
						if (!PonderRegistry.ALL.containsKey(RegisteredObjects.getKeyOrThrow(item)))
							return;

						centerScalingOn(x, y);
						ScreenOpener.transitionTo(PonderUI.of(new ItemStack(item)));
					});

			addRenderableWidget(b);
			layout.next();
		}

	}

	@Override
	protected void initBackTrackIcon(PonderButton backTrack) {
		backTrack.showing(AllItems.WRENCH.asStack());
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
		Window w = minecraft.getWindow();
		double mouseX = minecraft.mouseHandler.xpos() * w.getGuiScaledWidth() / w.getScreenWidth();
		double mouseY = minecraft.mouseHandler.ypos() * w.getGuiScaledHeight() / w.getScreenHeight();
		for (GuiEventListener child : children()) {
			if (child instanceof PonderButton button) {
				if (button.isMouseOver(mouseX, mouseY)) {
					hoveredItem = button.getItem();
				}
			}
		}
	}

	@Override
	protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		int x = (int) (width * chapterXmult);
		int y = (int) (height * chapterYmult);

		PoseStack ms = graphics.pose();
		
		if (!chapters.isEmpty()) {
			ms.pushPose();
			ms.translate(x, y, 0);

			UIRenderHelper.streak(graphics, 0, chapterArea.getX() - 10, chapterArea.getY() - 20, 20, 220);
			graphics.drawString(font, "Topics to Ponder about", chapterArea.getX() - 5, chapterArea.getY() - 25, Theme.i(Theme.Key.TEXT), false);

			ms.popPose();
		}

		x = (int) (width * itemXmult);
		y = (int) (height * itemYmult);

		ms.pushPose();
		ms.translate(x, y, 0);

		UIRenderHelper.streak(graphics, 0, itemArea.getX() - 10, itemArea.getY() - 20, 20, 220);
		graphics.drawString(font, "Items to inspect", itemArea.getX() - 5, itemArea.getY() - 25, Theme.i(Theme.Key.TEXT), false);

		ms.popPose();
	}

	@Override
	protected void renderWindowForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		if (hoveredItem.isEmpty())
			return;

		PoseStack ms = graphics.pose();
		ms.pushPose();
		ms.translate(0, 0, 200);

		graphics.renderTooltip(font, hoveredItem, mouseX, mouseY);

		ms.popPose();
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
				PonderButton btn = (PonderButton) w;
				btn.runCallback(x, y);
				handled.setTrue();
			}
		});

		if (handled.booleanValue())
			return true;
		return super.mouseClicked(x, y, button);
	}*/

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
