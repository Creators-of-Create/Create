package com.simibubi.create.content.logistics.block.inventories;

import static com.simibubi.create.foundation.gui.AllGuiTextures.ADJUSTABLE_CRATE;
import static com.simibubi.create.foundation.gui.AllGuiTextures.ADJUSTABLE_DOUBLE_CRATE;
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
	private final ITextComponent title = Lang.translate("gui.adjustable_crate.title");
	private final ITextComponent storageSpace = Lang.translate("gui.adjustable_crate.storageSpace");

	public AdjustableCrateScreen(AdjustableCrateContainer container, PlayerInventory inv, ITextComponent title) {
		super(container, inv, title);
		te = container.te;
		lastModification = -1;
	}

	@Override
	protected void init() {
		setWindowSize(PLAYER_INVENTORY.width + 100, ADJUSTABLE_CRATE.height + PLAYER_INVENTORY.height + 20);
		super.init();
		widgets.clear();

		allowedItemsLabel = new Label(guiLeft + 100 + 69, guiTop + 108, "").colored(0xfefefe)
			.withShadow();
		allowedItems = new ScrollInput(guiLeft + 100 + 65, guiTop + 104, 41, 14).titled(storageSpace.copy())
			.withRange(1, (container.doubleCrate ? 2049 : 1025))
			.writingTo(allowedItemsLabel)
			.withShiftStep(64)
			.setState(te.allowedAmount)
			.calling(s -> lastModification = 0);
		allowedItems.onChanged();
		widgets.add(allowedItemsLabel);
		widgets.add(allowedItems);

		extraAreas = new ArrayList<>();
		extraAreas.add(new Rectangle2d(guiLeft + ADJUSTABLE_CRATE.width + 110, guiTop + 46, 71, 70));
	}

	@Override
	protected void renderWindow(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		int crateLeft = guiLeft + 100;
		int crateTop = guiTop;
		int invLeft = guiLeft + 50;
		int invTop = crateTop + ADJUSTABLE_CRATE.height + 10;
		int fontColor = 0x4B3A22;

		if (container.doubleCrate) {
			crateLeft -= 72;
			ADJUSTABLE_DOUBLE_CRATE.draw(matrixStack, this, crateLeft, crateTop);
		} else
			ADJUSTABLE_CRATE.draw(matrixStack,this, crateLeft, crateTop);

		textRenderer.drawWithShadow(matrixStack, title, crateLeft - 3 + (ADJUSTABLE_CRATE.width - textRenderer.getWidth(title)) / 2,
			crateTop + 3, 0xfefefe);
		String itemCount = "" + te.itemCount;
		textRenderer.draw(matrixStack, itemCount, guiLeft + 100 + 53 - textRenderer.getStringWidth(itemCount), crateTop + 107, fontColor);

		PLAYER_INVENTORY.draw(matrixStack, this, invLeft, invTop);
		textRenderer.draw(matrixStack, playerInventory.getDisplayName(), invLeft + 7, invTop + 6, 0x666666);

		for (int slot = 0; slot < (container.doubleCrate ? 32 : 16); slot++) {
			if (allowedItems.getState() > slot * 64)
				continue;
			int slotsPerRow = (container.doubleCrate ? 8 : 4);
			int x = crateLeft + 22 + (slot % slotsPerRow) * 18;
			int y = crateTop + 19 + (slot / slotsPerRow) * 18;
			AllGuiTextures.ADJUSTABLE_CRATE_LOCKED_SLOT.draw(matrixStack, this, x, y);
		}

		GuiGameElement.of(renderedItem)
				.at(guiLeft + ADJUSTABLE_CRATE.width + 110, guiTop + 40)
				.scale(5)
				.render(matrixStack);
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
