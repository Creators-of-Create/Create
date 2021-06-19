package com.simibubi.create.content.logistics.block.inventories;

import static com.simibubi.create.foundation.gui.AllGuiTextures.ADJUSTABLE_CRATE;
import static com.simibubi.create.foundation.gui.AllGuiTextures.ADJUSTABLE_DOUBLE_CRATE;
import static com.simibubi.create.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.packet.ConfigureFlexcratePacket;
import com.simibubi.create.foundation.gui.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.widgets.Label;
import com.simibubi.create.foundation.gui.widgets.ScrollInput;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class AdjustableCrateScreen extends AbstractSimiContainerScreen<AdjustableCrateContainer> {

	protected AllGuiTextures background;
	private List<Rectangle2d> extraAreas = Collections.emptyList();

	private AdjustableCrateTileEntity te;
	private Label allowedItemsLabel;
	private ScrollInput allowedItems;
	private int lastModification;

	private int itemLabelOffset;
	private int textureXShift;
	private int itemYShift;

	private final ItemStack renderedItem = AllBlocks.ADJUSTABLE_CRATE.asStack();
	private final ITextComponent storageSpace = Lang.translate("gui.adjustable_crate.storageSpace");

	public AdjustableCrateScreen(AdjustableCrateContainer container, PlayerInventory inv, ITextComponent title) {
		super(container, inv, title);
		te = container.te;
		lastModification = -1;
		background = container.doubleCrate ? ADJUSTABLE_DOUBLE_CRATE : ADJUSTABLE_CRATE;
	}

	@Override
	protected void init() {
		setWindowSize(Math.max(background.width, PLAYER_INVENTORY.width), background.height + 4 + PLAYER_INVENTORY.height);
		setWindowOffset(container.doubleCrate ? -2 : 0, 0);
		super.init();
		widgets.clear();

		itemLabelOffset = container.doubleCrate ? 137 : 65;
		textureXShift = container.doubleCrate ? 0 : (xSize - (background.width - 8)) / 2;
		itemYShift = container.doubleCrate ? 0 : -16;

		int crateLeft = guiLeft + textureXShift;
		int crateTop = guiTop;

		allowedItemsLabel = new Label(crateLeft + itemLabelOffset + 4, crateTop + 108, StringTextComponent.EMPTY).colored(0xFFFFFF)
			.withShadow();
		allowedItems = new ScrollInput(crateLeft + itemLabelOffset, crateTop + 104, 41, 16).titled(storageSpace.copy())
			.withRange(1, (container.doubleCrate ? 2049 : 1025))
			.writingTo(allowedItemsLabel)
			.withShiftStep(64)
			.setState(te.allowedAmount)
			.calling(s -> lastModification = 0);
		allowedItems.onChanged();
		widgets.add(allowedItemsLabel);
		widgets.add(allowedItems);

		extraAreas = ImmutableList.of(
			new Rectangle2d(crateLeft + background.width, crateTop + background.height - 56 + itemYShift, 80, 80)
		);
	}

	@Override
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		int invLeft = guiLeft - windowXOffset + (xSize - AllGuiTextures.PLAYER_INVENTORY.width) / 2;
		int invTop = guiTop + background.height + 4;

		PLAYER_INVENTORY.draw(ms, this, invLeft, invTop);
		textRenderer.draw(ms, playerInventory.getDisplayName(), invLeft + 8, invTop + 6, 0x404040);

		int crateLeft = guiLeft + textureXShift;
		int crateTop = guiTop;

		background.draw(ms, this, crateLeft, crateTop);
		drawCenteredText(ms, textRenderer, title, crateLeft + (background.width - 8) / 2, crateTop + 3, 0xFFFFFF);

		String itemCount = String.valueOf(te.itemCount);
		textRenderer.draw(ms, itemCount, crateLeft + itemLabelOffset - 13 - textRenderer.getStringWidth(itemCount), crateTop + 108, 0x4B3A22);

		for (int slot = 0; slot < (container.doubleCrate ? 32 : 16); slot++) {
			if (allowedItems.getState() > slot * 64)
				continue;
			int slotsPerRow = (container.doubleCrate ? 8 : 4);
			int x = crateLeft + 22 + (slot % slotsPerRow) * 18;
			int y = crateTop + 19 + (slot / slotsPerRow) * 18;
			AllGuiTextures.ADJUSTABLE_CRATE_LOCKED_SLOT.draw(ms, this, x, y);
		}

		GuiGameElement.of(renderedItem)
				.<GuiGameElement.GuiRenderBuilder>at(crateLeft + background.width, crateTop + background.height - 56 + itemYShift, -200)
				.scale(5)
				.render(ms);
	}

	@Override
	public void removed() {
		AllPackets.channel.sendToServer(new ConfigureFlexcratePacket(te.getPos(), allowedItems.getState()));
	}

	@Override
	public void tick() {
		super.tick();

		if (!AllBlocks.ADJUSTABLE_CRATE.has(client.world.getBlockState(te.getPos())))
			client.displayGuiScreen(null);

		if (lastModification >= 0)
			lastModification++;

		if (lastModification >= 15) {
			lastModification = -1;
			AllPackets.channel.sendToServer(new ConfigureFlexcratePacket(te.getPos(), allowedItems.getState()));
		}

		if (container.doubleCrate != te.isDoubleCrate())
			container.playerInventory.player.closeScreen();
	}

	@Override
	public List<Rectangle2d> getExtraAreas() {
		return extraAreas;
	}

}
