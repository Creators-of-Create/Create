package com.simibubi.create.content.equipment.toolbox;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.render.LegacyRenderSystemBridge;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.createmod.catnip.api.platform.CatnipServices;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.api.client.gui.element.GuiGameElement;
import net.createmod.catnip.api.data.Iterate;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public class ToolboxScreen extends AbstractSimiContainerScreen<ToolboxMenu> {

	protected static final AllGuiTextures BG = AllGuiTextures.TOOLBOX;
	protected static final AllGuiTextures PLAYER = AllGuiTextures.PLAYER_INVENTORY;

	protected Slot hoveredToolboxSlot;
	private IconButton confirmButton;
	private IconButton disposeButton;
	private DyeColor color;

	private List<Rect2i> extraAreas = Collections.emptyList();

	public ToolboxScreen(ToolboxMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
		init();
	}

	@Override
	protected void init() {
		setWindowSize(30 + BG.getWidth(), BG.getHeight() + PLAYER.getHeight() - 24);
		setWindowOffset(-11, 0);
		super.init();
		clearWidgets();

		color = menu.contentHolder.getColor();

		confirmButton = new IconButton(leftPos + 30 + BG.getWidth() - 33, topPos + BG.getHeight() - 24, AllIcons.I_CONFIRM);
		confirmButton.withCallback(() -> {
			minecraft.player.closeContainer();
		});
		addRenderableWidget(confirmButton);

		disposeButton = new IconButton(leftPos + 30 + 81, topPos + 69, AllIcons.I_TOOLBOX);
		disposeButton.withCallback(() -> {
			net.createmod.catnip.api.client.network.ClientNetworkHelper.INSTANCE.sendToServer(new ToolboxDisposeAllPacket(menu.contentHolder.getBlockPos()));
		});
		disposeButton.setToolTip(CreateLang.translateDirect("toolbox.depositBox"));
		addRenderableWidget(disposeButton);

		extraAreas = ImmutableList.of(
			new Rect2i(leftPos + 30 + BG.getWidth(), topPos + BG.getHeight() - 15 - 34 - 6, 72, 68)
		);
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		menu.renderPass = true;
		super.render(graphics, mouseX, mouseY, partialTicks);
		menu.renderPass = false;
	}

	@Override
	protected void renderBg(GuiGraphicsExtractor graphics, float partialTicks, int mouseX, int mouseY) {
		int x = leftPos + imageWidth - BG.getWidth();
		int y = topPos;

		BG.render(graphics, x, y);
		graphics.text(font, title, x + 15, y + 4, 0x592424, false);

		int invX = leftPos;
		int invY = topPos + imageHeight - PLAYER.getHeight();
		renderPlayerInventory(graphics, invX, invY);

		renderToolbox(graphics, x + BG.getWidth() + 50, y + BG.getHeight() + 12, partialTicks);

		hoveredToolboxSlot = null;
		for (int compartment = 0; compartment < 8; compartment++) {
			int baseIndex = compartment * ToolboxInventory.STACKS_PER_COMPARTMENT;
			Slot slot = menu.slots.get(baseIndex);
			ItemStack itemstack = slot.getItem();
			int i = slot.x + leftPos;
			int j = slot.y + topPos;

			if (itemstack.isEmpty())
				itemstack = menu.getFilter(compartment);

			if (!itemstack.isEmpty()) {
				int count = menu.totalCountInCompartment(compartment);
				String s = String.valueOf(count);
				LegacyRenderSystemBridge.enableDepthTest();
				graphics.item(minecraft.player, itemstack, i, j, 0);
				graphics.itemDecorations(font, itemstack, i, j, s);
			}

			if (isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
				hoveredToolboxSlot = slot;
				LegacyRenderSystemBridge.disableDepthTest();
				LegacyRenderSystemBridge.colorMask(true, true, true, false);
				int slotColor = this.getSlotColor(baseIndex);
				graphics.fillGradient(i, j, i + 16, j + 16, slotColor, slotColor);
				LegacyRenderSystemBridge.colorMask(true, true, true, true);
				LegacyRenderSystemBridge.enableDepthTest();
			}
		}
	}

	private void renderToolbox(GuiGraphicsExtractor graphics, int x, int y, float partialTicks) {
		// TODO 26.2: Restore animated toolbox lid/drawer preview with GUI PIP rendering.
		GuiGameElement.of(AllBlocks.TOOLBOXES.get(color)
			.getDefaultState())
			.scale(50)
			.at(x, y)
			.render(graphics);
	}

	@Override
	protected void renderForeground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		if (hoveredToolboxSlot != null)
			hoveredSlot = hoveredToolboxSlot;
		super.renderForeground(graphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public List<Rect2i> getExtraAreas() {
		return extraAreas;
	}

}
