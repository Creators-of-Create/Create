package com.simibubi.create.content.logistics.item;

import static com.simibubi.create.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.gui.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public class LinkedControllerScreen extends AbstractSimiContainerScreen<LinkedControllerContainer> {

	protected AllGuiTextures background;
	private List<Rectangle2d> extraAreas = Collections.emptyList();

	private IconButton resetButton;
	private IconButton confirmButton;

	public LinkedControllerScreen(LinkedControllerContainer container, PlayerInventory inv, ITextComponent title) {
		super(container, inv, title);
		this.background = AllGuiTextures.LINKED_CONTROLLER;
	}

	@Override
	protected void init() {
		setWindowSize(background.width, background.height + 4 + PLAYER_INVENTORY.height);
		setWindowOffset(2 + (width % 2 == 0 ? 0 : -1), 0);
		super.init();
		widgets.clear();

		int x = leftPos;
		int y = topPos;

		resetButton = new IconButton(x + background.width - 62, y + background.height - 24, AllIcons.I_TRASH);
		confirmButton = new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);

		widgets.add(resetButton);
		widgets.add(confirmButton);

		extraAreas = ImmutableList.of(
			new Rectangle2d(x + background.width + 4, y + background.height - 44, 64, 56)
		);
	}

	@Override
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		int invX = getLeftOfCentered(PLAYER_INVENTORY.width);
		int invY = topPos + background.height + 4;
		renderPlayerInventory(ms, invX, invY);

		int x = leftPos;
		int y = topPos;

		background.draw(ms, this, x, y);
		font.draw(ms, title, x + 15, y + 4, 0x442000);

		GuiGameElement.of(menu.mainItem)
			.<GuiGameElement.GuiRenderBuilder>at(x + background.width - 4, y + background.height - 56, -200)
			.scale(5)
			.render(ms);
	}

	@Override
	public void tick() {
		super.tick();
		if (!menu.player.getMainHandItem()
			.equals(menu.mainItem, false))
			minecraft.player.closeContainer();
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		boolean mouseClicked = super.mouseClicked(x, y, button);

		if (button == 0) {
			if (confirmButton.isHovered()) {
				minecraft.player.closeContainer();
				return true;
			}
			if (resetButton.isHovered()) {
				menu.clearContents();
				menu.sendClearPacket();
				return true;
			}
		}

		return mouseClicked;
	}

	@Override
	protected void renderTooltip(MatrixStack ms, int x, int y) {
		if (!this.minecraft.player.inventory.getCarried()
			.isEmpty() || this.hoveredSlot == null || this.hoveredSlot.hasItem()
			|| hoveredSlot.container == menu.playerInventory) {
			super.renderTooltip(ms, x, y);
			return;
		}
		renderWrappedToolTip(ms, addToTooltip(new LinkedList<>(), hoveredSlot.getSlotIndex()), x, y, font);
	}

	@Override
	public List<ITextComponent> getTooltipFromItem(ItemStack stack) {
		List<ITextComponent> list = super.getTooltipFromItem(stack);
		if (hoveredSlot.container == menu.playerInventory)
			return list;
		return hoveredSlot != null ? addToTooltip(list, hoveredSlot.getSlotIndex()) : list;
	}

	private List<ITextComponent> addToTooltip(List<ITextComponent> list, int slot) {
		if (slot < 0 || slot >= 12)
			return list;
		list.add(Lang
			.createTranslationTextComponent("linked_controller.frequency_slot_" + ((slot % 2) + 1),
				LinkedControllerClientHandler.getControls()
					.get(slot / 2)
					.getTranslatedKeyMessage()
					.getString())
			.withStyle(TextFormatting.GOLD));
		return list;
	}

	@Override
	public List<Rectangle2d> getExtraAreas() {
		return extraAreas;
	}

}
