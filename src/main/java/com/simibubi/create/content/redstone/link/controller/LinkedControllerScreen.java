package com.simibubi.create.content.redstone.link.controller;

import static com.simibubi.create.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.ControlsUtil;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class LinkedControllerScreen extends AbstractSimiContainerScreen<LinkedControllerMenu> {

	protected AllGuiTextures background;
	private List<Rect2i> extraAreas = Collections.emptyList();

	private IconButton resetButton;
	private IconButton confirmButton;

	public LinkedControllerScreen(LinkedControllerMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
		this.background = AllGuiTextures.LINKED_CONTROLLER;
	}

	@Override
	protected void init() {
		setWindowSize(background.width, background.height + 4 + PLAYER_INVENTORY.height);
		setWindowOffset(1, 0);
		super.init();

		int x = leftPos;
		int y = topPos;

		resetButton = new IconButton(x + background.width - 62, y + background.height - 24, AllIcons.I_TRASH);
		resetButton.withCallback(() -> {
			menu.clearContents();
			menu.sendClearPacket();
		});
		confirmButton = new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);
		confirmButton.withCallback(() -> {
			minecraft.player.closeContainer();
		});

		addRenderableWidget(resetButton);
		addRenderableWidget(confirmButton);

		extraAreas = ImmutableList.of(new Rect2i(x + background.width + 4, y + background.height - 44, 64, 56));
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		int invX = getLeftOfCentered(PLAYER_INVENTORY.width);
		int invY = topPos + background.height + 4;
		renderPlayerInventory(graphics, invX, invY);

		int x = leftPos;
		int y = topPos;

		background.render(graphics, x, y);
		graphics.drawString(font, title, x + 15, y + 4, 0x592424, false);

		GuiGameElement.of(menu.contentHolder).<GuiGameElement
			.GuiRenderBuilder>at(x + background.width - 4, y + background.height - 56, -200)
			.scale(5)
			.render(graphics);
	}

	@Override
	protected void containerTick() {
		if (!menu.player.getMainHandItem()
			.equals(menu.contentHolder, false))
			menu.player.closeContainer();

		super.containerTick();
	}

	@Override
	protected void renderTooltip(GuiGraphics graphics, int x, int y) {
		if (!menu.getCarried()
			.isEmpty() || this.hoveredSlot == null || hoveredSlot.container == menu.playerInventory) {
			super.renderTooltip(graphics, x, y);
			return;
		}

		List<Component> list = new LinkedList<>();
		if (hoveredSlot.hasItem())
			list = getTooltipFromContainerItem(hoveredSlot.getItem());

		graphics.renderComponentTooltip(font, addToTooltip(list, hoveredSlot.getSlotIndex()), x, y);
	}

	private List<Component> addToTooltip(List<Component> list, int slot) {
		if (slot < 0 || slot >= 12)
			return list;
		list.add(Lang.translateDirect("linked_controller.frequency_slot_" + ((slot % 2) + 1), ControlsUtil.getControls()
			.get(slot / 2)
			.getTranslatedKeyMessage()
			.getString())
			.withStyle(ChatFormatting.GOLD));
		return list;
	}

	@Override
	public List<Rect2i> getExtraAreas() {
		return extraAreas;
	}

}
