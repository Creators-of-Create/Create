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
import net.minecraft.client.resources.I18n;
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
	protected void drawMouseoverTooltip(MatrixStack ms, int x, int y) {
		if (!this.client.player.inventory.getItemStack()
			.isEmpty() || this.hoveredSlot == null || this.hoveredSlot.getHasStack()) {
			super.drawMouseoverTooltip(ms, x, y);
			return;
		}
		renderWrappedToolTip(ms, addToTooltip(new LinkedList<>(), hoveredSlot.getSlotIndex()), x, y, textRenderer);
	}

	@Override
	public List<ITextComponent> getTooltipFromItem(ItemStack stack) {
		List<ITextComponent> list = super.getTooltipFromItem(stack);
		return hoveredSlot != null ? addToTooltip(list, hoveredSlot.getSlotIndex()) : list;
	}

	private List<ITextComponent> addToTooltip(List<ITextComponent> list, int slot) {
		if (slot < 0 || slot >= 12)
			return list;
		list.add(Lang
			.createTranslationTextComponent("linked_controller.frequency_slot_" + ((slot % 2) + 1),
				LinkedControllerClientHandler.getControls()
					.get(slot / 2)
					.getBoundKeyLocalizedText()
					.getString())
			.formatted(TextFormatting.GOLD));
		return list;
	}

	@Override
	protected void init() {
		setWindowSize(PLAYER_INVENTORY.width + 50, background.height + PLAYER_INVENTORY.height + 20);
		super.init();
		widgets.clear();
		int x = guiLeft - 50;
		int offset = guiTop < 30 ? 30 - guiTop : 0;
		extraAreas =
			ImmutableList.of(new Rectangle2d(x, guiTop + offset, background.width + 70, background.height - offset));

		resetButton = new IconButton(x + background.width - 12, guiTop + background.height - 14, AllIcons.I_TRASH);
		confirmButton = new IconButton(x + background.width + 16, guiTop + background.height - 14, AllIcons.I_CONFIRM);

		widgets.add(resetButton);
		widgets.add(confirmButton);
	}

	@Override
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		int x = guiLeft;
		int y = guiTop + 10;
		background.draw(ms, this, x, y);

		int invX = guiLeft + 14;
		int invY = y + background.height + 5;
		PLAYER_INVENTORY.draw(ms, this, invX, invY);
		textRenderer.draw(ms, playerInventory.getDisplayName(), invX + 7, invY + 6, 0x666666);
		textRenderer.draw(ms, I18n.format(container.mainItem.getTranslationKey()), x + 15, y + 4, 0x442000);

		GuiGameElement.of(container.mainItem).<GuiGameElement
			.GuiRenderBuilder>at(x + background.width - 8, guiTop + background.height - 53, -200)
			.scale(5)
			.render(ms);

	}

	@Override
	public void tick() {
		super.tick();
		if (!container.player.getHeldItemMainhand()
			.equals(container.mainItem, false))
			client.player.closeScreen();
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		boolean mouseClicked = super.mouseClicked(x, y, button);

		if (button == 0) {
			if (confirmButton.isHovered()) {
				client.player.closeScreen();
				return true;
			}
			if (resetButton.isHovered()) {
				container.clearContents();
				container.sendClearPacket();
				return true;
			}
		}

		return mouseClicked;
	}

	@Override
	public List<Rectangle2d> getExtraAreas() {
		return extraAreas;
	}
}
