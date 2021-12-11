package com.simibubi.create.content.schematics.block;

import com.simibubi.create.AllContainerTypes;
import com.simibubi.create.foundation.gui.container.ContainerBase;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.simibubi.create.lib.transfer.item.SlotItemHandler;

public class SchematicannonContainer extends ContainerBase<SchematicannonTileEntity> {

	public SchematicannonContainer(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf buffer) {
		super(type, id, inv, buffer);
	}

	public SchematicannonContainer(MenuType<?> type, int id, Inventory inv, SchematicannonTileEntity te) {
		super(type, id, inv, te);
	}

	public static SchematicannonContainer create(int id, Inventory inv, SchematicannonTileEntity te) {
		return new SchematicannonContainer(AllContainerTypes.SCHEMATICANNON.get(), id, inv, te);
	}

	@Override
	protected SchematicannonTileEntity createOnClient(FriendlyByteBuf extraData) {
		ClientLevel world = Minecraft.getInstance().level;
		BlockEntity tileEntity = world.getBlockEntity(extraData.readBlockPos());
		if (tileEntity instanceof SchematicannonTileEntity schematicannon) {
			schematicannon.readClient(extraData.readNbt());
			return schematicannon;
		}
		return null;
	}

	@Override
	protected void initAndReadInventory(SchematicannonTileEntity contentHolder) {
	}

	@Override
	protected void addSlots() {
		int x = 0;
		int y = 0;

		addSlot(new SlotItemHandler(contentHolder.inventory, 0, x + 15, y + 65));
		addSlot(new SlotItemHandler(contentHolder.inventory, 1, x + 171, y + 65));
		addSlot(new SlotItemHandler(contentHolder.inventory, 2, x + 134, y + 19));
		addSlot(new SlotItemHandler(contentHolder.inventory, 3, x + 174, y + 19));
		addSlot(new SlotItemHandler(contentHolder.inventory, 4, x + 15, y + 19));

		addPlayerSlots(37, 161);
	}

	@Override
	protected void saveData(SchematicannonTileEntity contentHolder) {
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		Slot clickedSlot = getSlot(index);
		if (!clickedSlot.hasItem())
			return ItemStack.EMPTY;
		ItemStack stack = clickedSlot.getItem();

		if (index < 5) {
			moveItemStackTo(stack, 5, slots.size(), false);
		} else {
			if (moveItemStackTo(stack, 0, 1, false) || moveItemStackTo(stack, 2, 3, false)
					|| moveItemStackTo(stack, 4, 5, false))
				;
		}

		return ItemStack.EMPTY;
	}

}
