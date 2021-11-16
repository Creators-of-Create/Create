package com.simibubi.create.content.logistics.block.inventories;

import com.simibubi.create.AllContainerTypes;
import com.simibubi.create.foundation.gui.container.ContainerBase;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.simibubi.create.lib.transfer.item.SlotItemHandler;

public class AdjustableCrateContainer extends ContainerBase<AdjustableCrateTileEntity> {

	protected boolean doubleCrate;

	public AdjustableCrateContainer(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
		super(type, id, inv, extraData);
	}

	public AdjustableCrateContainer(MenuType<?> type, int id, Inventory inv, AdjustableCrateTileEntity te) {
		super(type, id, inv, te);
	}

	public static AdjustableCrateContainer create(int id, Inventory inv, AdjustableCrateTileEntity te) {
		return new AdjustableCrateContainer(AllContainerTypes.FLEXCRATE.get(), id, inv, te);
	}

	@Override
	protected AdjustableCrateTileEntity createOnClient(FriendlyByteBuf extraData) {
		BlockPos readBlockPos = extraData.readBlockPos();
		CompoundTag readNbt = extraData.readNbt();

		ClientLevel world = Minecraft.getInstance().level;
		BlockEntity tileEntity = world.getBlockEntity(readBlockPos);
		if (tileEntity instanceof AdjustableCrateTileEntity crate) {
			crate.handleUpdateTag(readNbt);
			return crate;
		}

		return null;
	}

	@Override
	protected void initAndReadInventory(AdjustableCrateTileEntity contentHolder) {
		doubleCrate = contentHolder.isDoubleCrate();
	}

	@Override
	protected void addSlots() {
		int x = doubleCrate ? 23 : 53;
		int maxCol = doubleCrate ? 8 : 4;
		for (int row = 0; row < 4; ++row) {
			for (int col = 0; col < maxCol; ++col) {
				this.addSlot(new SlotItemHandler(contentHolder.inventory, col + row * maxCol, x + col * 18, 20 + row * 18));
			}
		}

		addPlayerSlots(doubleCrate ? 20 : 8, 149);
	}

	@Override
	protected void saveData(AdjustableCrateTileEntity contentHolder) {
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		Slot clickedSlot = getSlot(index);
		if (!clickedSlot.hasItem())
			return ItemStack.EMPTY;

		ItemStack stack = clickedSlot.getItem();
		int crateSize = doubleCrate ? 32 : 16;
		if (index < crateSize) {
			moveItemStackTo(stack, crateSize, slots.size(), false);
			contentHolder.inventory.onContentsChanged(index);
		} else
			moveItemStackTo(stack, 0, crateSize - 1, false);

		return ItemStack.EMPTY;
	}

}
