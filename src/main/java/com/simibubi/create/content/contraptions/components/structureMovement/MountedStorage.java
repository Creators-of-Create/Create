package com.simibubi.create.content.contraptions.components.structureMovement;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.logistics.block.inventories.AdjustableCrateBlock;

import net.minecraft.block.ChestBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.BarrelTileEntity;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.ShulkerBoxTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public class MountedStorage {

	private static final ItemStackHandler dummyHandler = new ItemStackHandler();

	ItemStackHandler handler;
	boolean working;
	private TileEntity te;

	public MountedStorage(TileEntity te) {
		this.te = te;
		handler = dummyHandler;
	}

	public void empty() {
		working = false;
		if (te == null)
			return;

		// Split double chests
		if (te.getType() == TileEntityType.CHEST || te.getType() == TileEntityType.TRAPPED_CHEST) {
			if (te.getBlockState()
				.get(ChestBlock.TYPE) != ChestType.SINGLE)
				te.getWorld()
					.setBlockState(te.getPos(), te.getBlockState()
						.with(ChestBlock.TYPE, ChestType.SINGLE));
			te.updateContainingBlockInfo();
		}

		// Split double flexcrates
		if (AllTileEntities.ADJUSTABLE_CRATE.is(te)) {
			if (te.getBlockState()
				.get(AdjustableCrateBlock.DOUBLE))
				te.getWorld()
					.setBlockState(te.getPos(), te.getBlockState()
						.with(AdjustableCrateBlock.DOUBLE, false));
			te.updateContainingBlockInfo();
		}

		IItemHandler teHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			.orElse(dummyHandler);
		if (teHandler == dummyHandler)
			return;

		// te uses ItemStackHandler
		if (teHandler instanceof ItemStackHandler) {
			handler = (ItemStackHandler) teHandler;
			working = true;
			return;
		}

		// serialization not accessible -> fill into a serializable handler
		if (teHandler instanceof IItemHandlerModifiable) {
			IItemHandlerModifiable inv = (IItemHandlerModifiable) teHandler;
			handler = new ItemStackHandler(teHandler.getSlots());
			for (int slot = 0; slot < handler.getSlots(); slot++) {
				handler.setStackInSlot(slot, inv.getStackInSlot(slot));
				inv.setStackInSlot(slot, ItemStack.EMPTY);
			}
			working = true;
			return;
		}

	}

	public MountedStorage(CompoundNBT nbt) {
		handler = new ItemStackHandler();
		working = nbt != null;
		if (working) 
			handler.deserializeNBT(nbt);
	}

	public void fill(TileEntity te) {
		IItemHandler teHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			.orElse(dummyHandler);
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
		if (AllTileEntities.ADJUSTABLE_CRATE.is(te))
			return true;
		if (te instanceof ShulkerBoxTileEntity)
			return true;
		if (te instanceof ChestTileEntity)
			return true;
		if (te instanceof BarrelTileEntity)
			return true;
		LazyOptional<IItemHandler> capability = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		if (capability.isPresent() && capability.orElse(null) instanceof ItemStackHandler)
			return true;
		return false;
	}

}
