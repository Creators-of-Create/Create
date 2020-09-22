package com.simibubi.create.content.logistics.block.inventories;

import static com.simibubi.create.foundation.gui.AllGuiTextures.FLEXCRATE;
import static com.simibubi.create.foundation.gui.AllGuiTextures.FLEXCRATE_DOUBLE;
import static com.simibubi.create.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;

import java.util.ArrayList;
import java.util.List;

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

public class AdjustableCrateScreen extends AbstractSimiContainerScreen<AdjustableCrateContainer> {

	private AdjustableCrateTileEntity te;
	private Label allowedItemsLabel;
	private ScrollInput allowedItems;
	private int lastModification;

	private List<Rectangle2d> extraAreas;

	private final ItemStack renderedItem = AllBlocks.ADJUSTABLE_CRATE.asStack();
	private final String title = Lang.translate("gui.adjustable_crate.title");
	private final String storageSpace = Lang.translate("gui.adjustable_crate.storageSpace");

	public AdjustableCrateScreen(AdjustableCrateContainer container, PlayerInventory inv, ITextComponent title) {
		super(container, inv, title);
		te = container.te;
		lastModification = -1;
	}

	@Override
	protected void init() {
		setWindowSize(PLAYER_INVENTORY.width + 100, FLEXCRATE.height + PLAYER_INVENTORY.height + 20);
		super.init();
		widgets.clear();

		allowedItemsLabel = new Label(guiLeft + 100 + 70, guiTop + 107, "").colored(0xD3CBBE)
			.withShadow();
		allowedItems = new ScrollInput(guiLeft + 100 + 65, guiTop + 104, 41, 14).titled(storageSpace)
			.withRange(1, (container.doubleCrate ? 2049 : 1025))
			.writingTo(allowedItemsLabel)
			.withShiftStep(64)
			.setState(te.allowedAmount)
			.calling(s -> lastModification = 0);
		allowedItems.onChanged();
		widgets.add(allowedItemsLabel);
		widgets.add(allowedItems);

		extraAreas = new ArrayList<>();
		extraAreas.add(new Rectangle2d(guiLeft + FLEXCRATE.width + 110, guiTop + 46, 71, 70));
	}

	@Override
	protected void renderWindow(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		int crateLeft = guiLeft + 100;
		int crateTop = guiTop;
		int invLeft = guiLeft + 50;
		int invTop = crateTop + FLEXCRATE.height + 10;
		int hFontColor = 0xD3CBBE;
		int fontColor = 0x4B3A22;

		if (container.doubleCrate) {
			crateLeft -= 72;
			FLEXCRATE_DOUBLE.draw(this, crateLeft, crateTop);
		} else
			FLEXCRATE.draw(this, crateLeft, crateTop);

		textRenderer.drawWithShadow(matrixStack, title, crateLeft - 3 + (FLEXCRATE.width - textRenderer.getStringWidth(title)) / 2,
			crateTop + 10, hFontColor);
		String itemCount = "" + te.itemCount;
		textRenderer.draw(matrixStack, itemCount, guiLeft + 100 + 53 - textRenderer.getStringWidth(itemCount), crateTop + 107, fontColor);

		PLAYER_INVENTORY.draw(this, invLeft, invTop);
		textRenderer.draw(matrixStack, playerInventory.getDisplayName(), invLeft + 7, invTop + 6, 0x666666);

		for (int slot = 0; slot < (container.doubleCrate ? 32 : 16); slot++) {
			if (allowedItems.getState() > slot * 64)
				continue;
			int slotsPerRow = (container.doubleCrate ? 8 : 4);
			int x = crateLeft + 23 + (slot % slotsPerRow) * 18;
			int y = crateTop + 24 + (slot / slotsPerRow) * 18;
			AllGuiTextures.FLEXCRATE_LOCKED_SLOT.draw(this, x, y);
		}

		GuiGameElement.of(renderedItem)
				.at(guiLeft + FLEXCRATE.width + 110, guiTop + 40)
				.scale(5)
				.render();
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
