package com.simibubi.create.content.contraptions.components.structureMovement;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterTileEntity;
import com.simibubi.create.content.contraptions.processing.ProcessingInventory;
import com.simibubi.create.content.logistics.block.inventories.BottomlessItemHandler;
import com.simibubi.create.content.logistics.block.vault.ItemVaultTileEntity;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public class MountedStorage {

	private static final ItemStackHandler dummyHandler = new ItemStackHandler();

	ItemStackHandler handler;
	boolean noFuel;
	boolean valid;

	private BlockEntity te;

	public static boolean canUseAsStorage(BlockEntity te) {
		if (te == null)
			return false;
		if (te instanceof MechanicalCrafterTileEntity)
			return false;
		if (AllTileEntities.CREATIVE_CRATE.is(te))
			return true;
		if (te instanceof ShulkerBoxBlockEntity)
			return true;
		if (te instanceof ChestBlockEntity)
			return true;
		if (te instanceof BarrelBlockEntity)
			return true;
		if (te instanceof ItemVaultTileEntity)
			return true;

		LazyOptional<IItemHandler> capability = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		IItemHandler handler = capability.orElse(null);
		return handler instanceof ItemStackHandler && !(handler instanceof ProcessingInventory);
	}

	public MountedStorage(BlockEntity te) {
		this.te = te;
		handler = dummyHandler;
		noFuel = te instanceof ItemVaultTileEntity;
	}

	public void removeStorageFromWorld() {
		valid = false;
		if (te == null)
			return;

		if (te instanceof ChestBlockEntity) {
			CompoundTag tag = te.saveWithFullMetadata();
			if (tag.contains("LootTable", 8))
				return;

			handler = new ItemStackHandler(((ChestBlockEntity) te).getContainerSize());
			NonNullList<ItemStack> items = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
			ContainerHelper.loadAllItems(tag, items);
			for (int i = 0; i < items.size(); i++)
				handler.setStackInSlot(i, items.get(i));
			valid = true;
			return;
		}

		IItemHandler teHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			.orElse(dummyHandler);
		if (teHandler == dummyHandler)
			return;

		// multiblock vaults need to provide individual invs
		if (te instanceof ItemVaultTileEntity) {
			handler = ((ItemVaultTileEntity) te).getInventoryOfBlock();
			valid = true;
			return;
		}

		// te uses ItemStackHandler
		if (teHandler instanceof ItemStackHandler) {
			handler = (ItemStackHandler) teHandler;
			valid = true;
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
			valid = true;
			return;
		}

	}

	public void addStorageToWorld(BlockEntity te) {
		// FIXME: More dynamic mounted storage in .4
		if (handler instanceof BottomlessItemHandler)
			return;

		if (te instanceof ChestBlockEntity) {
			CompoundTag tag = te.saveWithFullMetadata();
			tag.remove("Items");
			NonNullList<ItemStack> items = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
			for (int i = 0; i < items.size(); i++)
				items.set(i, handler.getStackInSlot(i));
			ContainerHelper.saveAllItems(tag, items);
			te.load(tag);
			return;
		}

		if (te instanceof ItemVaultTileEntity) {
			((ItemVaultTileEntity) te).applyInventoryToBlock(handler);
			return;
		}

		LazyOptional<IItemHandler> capability = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		IItemHandler teHandler = capability.orElse(null);
		if (!(teHandler instanceof IItemHandlerModifiable))
			return;

		IItemHandlerModifiable inv = (IItemHandlerModifiable) teHandler;
		for (int slot = 0; slot < Math.min(inv.getSlots(), handler.getSlots()); slot++)
			inv.setStackInSlot(slot, handler.getStackInSlot(slot));
	}

	public IItemHandlerModifiable getItemHandler() {
		return handler;
	}

	public CompoundTag serialize() {
		if (!valid)
			return null;

		CompoundTag tag = handler.serializeNBT();
		if (noFuel)
			NBTHelper.putMarker(tag, "NoFuel");
		if (!(handler instanceof BottomlessItemHandler))
			return tag;

		NBTHelper.putMarker(tag, "Bottomless");
		tag.put("ProvidedStack", handler.getStackInSlot(0)
			.serializeNBT());
		return tag;
	}

	public static MountedStorage deserialize(CompoundTag nbt) {
		MountedStorage storage = new MountedStorage(null);
		storage.handler = new ItemStackHandler();
		if (nbt == null)
			return storage;
		storage.valid = true;
		storage.noFuel = nbt.contains("NoFuel");

		if (nbt.contains("Bottomless")) {
			ItemStack providedStack = ItemStack.of(nbt.getCompound("ProvidedStack"));
			storage.handler = new BottomlessItemHandler(() -> providedStack);
			return storage;
		}

		storage.handler.deserializeNBT(nbt);
		return storage;
	}

	public boolean isValid() {
		return valid;
	}
	
	public boolean canUseForFuel() {
		return !noFuel;
	}

}
