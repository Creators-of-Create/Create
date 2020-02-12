package com.simibubi.create.modules.contraptions.components.contraptions;

import com.simibubi.create.AllTileEntities;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public class MountedStorage {

	private static final ItemStackHandler dummyHandler = new ItemStackHandler();

	ItemStackHandler handler;
	boolean working;

	public MountedStorage(TileEntity te) {
		handler = dummyHandler;
		if (te != null) {
			IItemHandler teHandler =
				te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(dummyHandler);
			if (teHandler != dummyHandler && teHandler instanceof IItemHandlerModifiable) {
				IItemHandlerModifiable inv = (IItemHandlerModifiable) teHandler;
				handler = new ItemStackHandler(teHandler.getSlots());
				for (int slot = 0; slot < handler.getSlots(); slot++) {
					handler.setStackInSlot(slot, inv.getStackInSlot(slot));
					inv.setStackInSlot(slot, ItemStack.EMPTY);
				}
			}
		}

		working = te != null && handler != dummyHandler;
	}

	public MountedStorage(CompoundNBT nbt) {
		handler = new ItemStackHandler();
		working = nbt != null;
		if (working)
			handler.deserializeNBT(nbt);
	}

	public void fill(TileEntity te) {
		IItemHandler teHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(dummyHandler);
		if (teHandler != dummyHandler && teHandler instanceof IItemHandlerModifiable) {
			IItemHandlerModifiable inv = (IItemHandlerModifiable) teHandler;
			for (int slot = 0; slot < Math.min(inv.getSlots(), handler.getSlots()); slot++)
				inv.setStackInSlot(slot, handler.getStackInSlot(slot));
		}
	}

	public IItemHandlerModifiable getItemHandler() {
		return handler;
	}

	public CompoundNBT serialize() {
		return working ? handler.serializeNBT() : null;
	}

	public boolean isWorking() {
		return working;
	}

	public static boolean canUseAsStorage(TileEntity te) {
		if (te == null)
			return false;
		TileEntityType<?> type = te.getType();
		if (type == TileEntityType.CHEST || type == TileEntityType.SHULKER_BOX || type == TileEntityType.BARREL)
			return true;
		if (type == AllTileEntities.FLEXCRATE.type)
			return true;

		return false;
	}

}
