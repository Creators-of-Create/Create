package com.simibubi.create.content.logistics.block.inventories;

import static com.simibubi.create.foundation.gui.AllGuiTextures.ADJUSTABLE_CRATE;
import static com.simibubi.create.foundation.gui.AllGuiTextures.ADJUSTABLE_DOUBLE_CRATE;
import static com.simibubi.create.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.packet.ConfigureFlexcratePacket;
import com.simibubi.create.foundation.gui.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.widgets.Label;
import com.simibubi.create.foundation.gui.widgets.ScrollInput;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class AdjustableCrateScreen extends AbstractSimiContainerScreen<AdjustableCrateContainer> {

	protected AllGuiTextures background;
	private List<Rect2i> extraAreas = Collections.emptyList();

	private AdjustableCrateTileEntity te;
	private Label allowedItemsLabel;
	private ScrollInput allowedItems;
	private int lastModification;

	private int itemLabelOffset;
	private int textureXShift;
	private int itemYShift;

	private final ItemStack renderedItem = AllBlocks.ADJUSTABLE_CRATE.asStack();
	private final Component storageSpace = Lang.translate("gui.adjustable_crate.storageSpace");

	public AdjustableCrateScreen(AdjustableCrateContainer container, Inventory inv, Component title) {
		super(container, inv, title);
		te = container.te;
		lastModification = -1;
		background = container.doubleCrate ? ADJUSTABLE_DOUBLE_CRATE : ADJUSTABLE_CRATE;
	}

	@Override
	protected void init() {
		setWindowSize(Math.max(background.width, PLAYER_INVENTORY.width), background.height + 4 + PLAYER_INVENTORY.height);
		setWindowOffset(menu.doubleCrate ? -2 : 0, 0);
		super.init();
		widgets.clear();

		itemLabelOffset = menu.doubleCrate ? 137 : 65;
		textureXShift = menu.doubleCrate ? 0 : (imageWidth - (background.width - 8)) / 2;
		itemYShift = menu.doubleCrate ? 0 : -16;

		int x = leftPos + textureXShift;
		int y = topPos;

		allowedItemsLabel = new Label(x + itemLabelOffset + 4, y + 108, TextComponent.EMPTY).colored(0xFFFFFF)
			.withShadow();
		allowedItems = new ScrollInput(x + itemLabelOffset, y + 104, 41, 16).titled(storageSpace.plainCopy())
			.withRange(1, (menu.doubleCrate ? 2049 : 1025))
			.writingTo(allowedItemsLabel)
			.withShiftStep(64)
			.setState(te.allowedAmount)
			.calling(s -> lastModification = 0);
		allowedItems.onChanged();
		widgets.add(allowedItemsLabel);
		widgets.add(allowedItems);

		extraAreas = ImmutableList.of(
			new Rect2i(x + background.width, y + background.height - 56 + itemYShift, 80, 80)
		);
	}

	@Override
	protected void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		int invX = getLeftOfCentered(PLAYER_INVENTORY.width);
		int invY = topPos + background.height + 4;
		renderPlayerInventory(ms, invX, invY);

		int x = leftPos + textureXShift;
		int y = topPos;

		background.draw(ms, this, x, y);
		drawCenteredString(ms, font, title, x + (background.width - 8) / 2, y + 3, 0xFFFFFF);

		String itemCount = String.valueOf(te.itemCount);
		font.draw(ms, itemCount, x + itemLabelOffset - 13 - font.width(itemCount), y + 108, 0x4B3A22);

		for (int slot = 0; slot < (menu.doubleCrate ? 32 : 16); slot++) {
			if (allowedItems.getState() > slot * 64)
				continue;
			int slotsPerRow = (menu.doubleCrate ? 8 : 4);
			int slotX = x + 22 + (slot % slotsPerRow) * 18;
			int slotY = y + 19 + (slot / slotsPerRow) * 18;
			AllGuiTextures.ADJUSTABLE_CRATE_LOCKED_SLOT.draw(ms, this, slotX, slotY);
		}

		GuiGameElement.of(renderedItem)
				.<GuiGameElement.GuiRenderBuilder>at(x + background.width, y + background.height - 56 + itemYShift, -200)
				.scale(5)
				.render(ms);
	}

	@Override
	public void removed() {
		AllPackets.channel.sendToServer(new ConfigureFlexcratePacket(te.getBlockPos(), allowedItems.getState()));
	}

	@Override
	public void tick() {
		super.tick();

		if (!AllBlocks.ADJUSTABLE_CRATE.has(minecraft.level.getBlockState(te.getBlockPos())))
			minecraft.setScreen(null);

		if (lastModification >= 0)
			lastModification++;

		if (lastModification >= 15) {
			lastModification = -1;
			AllPackets.channel.sendToServer(new ConfigureFlexcratePacket(te.getBlockPos(), allowedItems.getState()));
		}

		if (menu.doubleCrate != te.isDoubleCrate())
			menu.playerInventory.player.closeContainer();
	}

	@Override
	public List<Rect2i> getExtraAreas() {
		return extraAreas;
	}

}
