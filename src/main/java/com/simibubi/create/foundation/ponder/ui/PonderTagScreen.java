package com.simibubi.create.foundation.ponder.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.gui.Theme;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.gui.element.BoxElement;
import com.simibubi.create.foundation.ponder.PonderChapter;
import com.simibubi.create.foundation.ponder.PonderLocalization;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import com.simibubi.create.foundation.ponder.PonderTag;
import com.simibubi.create.foundation.utility.FontHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.RegisteredObjects;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class PonderTagScreen extends NavigatableSimiScreen {

	public static final String ASSOCIATED = PonderLocalization.LANG_PREFIX + "associated";

	private final PonderTag tag;
	protected final List<Item> items;
	private final double itemXmult = 0.5;
	protected Rect2i itemArea;
	protected final List<PonderChapter> chapters;
	private final double chapterXmult = 0.5;
	private final double chapterYmult = 0.75;
	protected Rect2i chapterArea;
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

		// items
		items.clear();
		PonderRegistry.TAGS.getItems(tag)
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

		int rowCount = Mth.clamp((int) Math.ceil(items.size() / 11d), 1, 3);
		LayoutHelper layout = LayoutHelper.centeredHorizontal(items.size(), rowCount, 28, 28, 8);
		itemArea = layout.getArea();
		int itemCenterX = (int) (width * itemXmult);
		int itemCenterY = getItemsY();

		for (Item i : items) {
			PonderButton b = new PonderButton(itemCenterX + layout.getX() + 4, itemCenterY + layout.getY() + 4)
					.showing(new ItemStack(i));

			if (PonderRegistry.ALL.containsKey(RegisteredObjects.getKeyOrThrow(i))) {
				b.withCallback((mouseX, mouseY) -> {
					centerScalingOn(mouseX, mouseY);
					ScreenOpener.transitionTo(PonderUI.of(new ItemStack(i), tag));
				});
			} else {
				if (RegisteredObjects.getKeyOrThrow(i)
						.getNamespace()
						.equals(Create.ID))
					b.withBorderColors(Theme.p(Theme.Key.PONDER_MISSING_CREATE))
							.animateColors(false);
				else
					b.withBorderColors(Theme.p(Theme.Key.PONDER_MISSING_VANILLA))
							.animateColors(false);
			}

			addRenderableWidget(b);
			layout.next();
		}

		if (!tag.getMainItem().isEmpty()) {
			ResourceLocation registryName = RegisteredObjects.getKeyOrThrow(tag.getMainItem()
					.getItem());

			PonderButton b = new PonderButton(itemCenterX - layout.getTotalWidth() / 2 - 48, itemCenterY - 10)
					.showing(tag.getMainItem());
			b.withCustomBackground(Theme.c(Theme.Key.PONDER_BACKGROUND_IMPORTANT));

			if (PonderRegistry.ALL.containsKey(registryName)) {
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

			addRenderableWidget(b);
		}

		// chapters
		chapters.clear();
		chapters.addAll(PonderRegistry.TAGS.getChapters(tag));

		rowCount = Mth.clamp((int) Math.ceil(chapters.size() / 3f), 1, 3);
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

			addRenderableWidget(label);
			layout.next();
		}

	}

	@Override
	protected void initBackTrackIcon(PonderButton backTrack) {
		backTrack.showing(tag);
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
			if (child == backTrack)
				continue;
			if (child instanceof PonderButton button)
				if (button.isMouseOver(mouseX, mouseY)) {
					hoveredItem = button.getItem();
				}
		}
	}

	@Override
	protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		renderItems(graphics, mouseX, mouseY, partialTicks);

		renderChapters(graphics, mouseX, mouseY, partialTicks);

		PoseStack ms = graphics.pose();
		ms.pushPose();
		ms.translate(width / 2 - 120, height * mainYmult - 40, 0);

		ms.pushPose();
		//ms.translate(0, 0, 800);
		int x = 31 + 20 + 8;
		int y = 31;

		String title = tag.getTitle();

		int streakHeight = 35;
		UIRenderHelper.streak(graphics, 0, x - 4, y - 12 + streakHeight / 2, streakHeight, 240);
		//PonderUI.renderBox(ms, 21, 21, 30, 30, false);
		new BoxElement()
				.withBackground(Theme.c(Theme.Key.PONDER_BACKGROUND_FLAT))
				.gradientBorder(Theme.p(Theme.Key.PONDER_IDLE))
				.at(21, 21, 100)
				.withBounds(30, 30)
				.render(graphics);

		graphics.drawString(font, Lang.translateDirect(PonderUI.PONDERING), x, y - 6, Theme.i(Theme.Key.TEXT_DARKER), false);
		y += 8;
		x += 0;
		ms.translate(x, y, 0);
		ms.translate(0, 0, 5);
		graphics.drawString(font, title, 0, 0, Theme.i(Theme.Key.TEXT), false);
		ms.popPose();

		ms.pushPose();
		ms.translate(23, 23, 10);
		ms.scale(1.66f, 1.66f, 1.66f);
		tag.render(graphics, 0, 0);
		ms.popPose();
		ms.popPose();

		ms.pushPose();
		int w = (int) (width * .45);
		x = (width - w) / 2;
		y = getItemsY() - 10 + Math.max(itemArea.getHeight(), 48);

		String desc = tag.getDescription();
		int h = font.wordWrapHeight(desc, w);


		//PonderUI.renderBox(ms, x - 3, y - 3, w + 6, h + 6, false);
		new BoxElement()
				.withBackground(Theme.c(Theme.Key.PONDER_BACKGROUND_FLAT))
				.gradientBorder(Theme.p(Theme.Key.PONDER_IDLE))
				.at(x - 3, y - 3, 90)
				.withBounds(w + 6, h + 6)
				.render(graphics);

		ms.translate(0, 0, 100);
		FontHelper.drawSplitString(ms, font, desc, x, y, w, Theme.i(Theme.Key.TEXT));
		ms.popPose();
	}

	protected void renderItems(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		if (items.isEmpty())
			return;

		int x = (int) (width * itemXmult);
		int y = getItemsY();

		String relatedTitle = Lang.translateDirect(ASSOCIATED).getString();
		int stringWidth = font.width(relatedTitle);

		PoseStack ms = graphics.pose();
		ms.pushPose();
		ms.translate(x, y, 0);
		//PonderUI.renderBox(ms, (sWidth - stringWidth) / 2 - 5, itemArea.getY() - 21, stringWidth + 10, 10, false);
		new BoxElement()
				.withBackground(Theme.c(Theme.Key.PONDER_BACKGROUND_FLAT))
				.gradientBorder(Theme.p(Theme.Key.PONDER_IDLE))
				.at((windowWidth - stringWidth) / 2f - 5, itemArea.getY() - 21, 100)
				.withBounds(stringWidth + 10, 10)
				.render(graphics);

		ms.translate(0, 0, 200);

//		UIRenderHelper.streak(0, itemArea.getX() - 10, itemArea.getY() - 20, 20, 180, 0x101010);
		graphics.drawCenteredString(font, relatedTitle, windowWidth / 2, itemArea.getY() - 20, Theme.i(Theme.Key.TEXT));

		ms.translate(0, 0, -200);

		UIRenderHelper.streak(graphics, 0, 0, 0, itemArea.getHeight() + 10, itemArea.getWidth() / 2 + 75);
		UIRenderHelper.streak(graphics, 180, 0, 0, itemArea.getHeight() + 10, itemArea.getWidth() / 2 + 75);

		ms.popPose();

	}

	public int getItemsY() {
		return (int) (mainYmult * height + 85);
	}

	protected void renderChapters(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		if (chapters.isEmpty())
			return;

		int chapterX = (int) (width * chapterXmult);
		int chapterY = (int) (height * chapterYmult);

		PoseStack ms = graphics.pose();
		ms.pushPose();
		ms.translate(chapterX, chapterY, 0);

		UIRenderHelper.streak(graphics, 0, chapterArea.getX() - 10, chapterArea.getY() - 20, 20, 220);
		graphics.drawString(font, "More Topics to Ponder about", chapterArea.getX() - 5, chapterArea.getY() - 25, Theme.i(Theme.Key.TEXT_ACCENT_SLIGHT), false);

		ms.popPose();
	}

	@Override
	protected void renderWindowForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		RenderSystem.disableDepthTest();
		PoseStack ms = graphics.pose();
		ms.pushPose();
		ms.translate(0, 0, 200);

		if (!hoveredItem.isEmpty()) {
			graphics.renderTooltip(font, hoveredItem, mouseX, mouseY);
		}

		ms.popPose();
		RenderSystem.enableDepthTest();
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
