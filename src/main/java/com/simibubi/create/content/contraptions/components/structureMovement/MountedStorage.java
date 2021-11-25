package com.simibubi.create.content.contraptions.components.structureMovement;

import com.simibubi.create.api.contraption.ContraptionItemStackHandler;
import com.simibubi.create.api.contraption.ContraptionStorageRegistry;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterTileEntity;
import com.simibubi.create.content.contraptions.processing.ProcessingInventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import static com.simibubi.create.api.contraption.ContraptionStorageRegistry.dummyHandler;

public class MountedStorage {

	ItemStackHandler handler;
	boolean valid;
	private TileEntity te;

	public static boolean canUseAsStorage(TileEntity te) {
		if (te == null)
			return false;

		if (te instanceof MechanicalCrafterTileEntity)
			return false;

		ContraptionStorageRegistry registry = ContraptionStorageRegistry.forTileEntity(te.getType());
		if (registry != null) return registry.canUseAsStorage(te);

		LazyOptional<IItemHandler> capability = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		IItemHandler handler = capability.orElse(null);
		return handler instanceof ItemStackHandler && !(handler instanceof ProcessingInventory);
	}

	public MountedStorage(TileEntity te) {
		this.te = te;
		handler = dummyHandler;
	}

	public void removeStorageFromWorld() {
		valid = false;
		if (te == null)
			return;

		ContraptionStorageRegistry registry = ContraptionStorageRegistry.forTileEntity(te.getType());
		if (registry == null) return;
		IItemHandler teHandler = registry.createHandler(te);
		if (teHandler != null) {
			handler = (ContraptionItemStackHandler) teHandler;
			valid = true;
			return;
		}

		teHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			.orElse(dummyHandler);
		if (teHandler == dummyHandler)
			return;

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

	public void addStorageToWorld(TileEntity te) {

		if (handler instanceof ContraptionItemStackHandler) {
			boolean cancel = !((ContraptionItemStackHandler) handler).addStorageToWorld(te);
			if (cancel) {
				return;
			}
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

	public CompoundNBT serialize() {
		if (!valid)
			return null;

		return handler.serializeNBT();
	}

	public static MountedStorage deserialize(World world, CompoundNBT nbt) {
		MountedStorage storage = new MountedStorage(null);
		storage.handler = new ItemStackHandler();
		if (nbt == null)
			return storage;

		if (nbt.contains(ContraptionStorageRegistry.REGISTRY_NAME)) {
			String id = nbt.getString(ContraptionStorageRegistry.REGISTRY_NAME);
			ContraptionStorageRegistry registry = ContraptionStorageRegistry.REGISTRY.get().getValue(new ResourceLocation(id));
			if (registry != null) {
				storage.handler = registry.deserializeHandler(nbt);
				if (storage.handler == null) storage.handler = dummyHandler;
				else {
					((ContraptionItemStackHandler) storage.handler).applyWorld(world);
					storage.valid = true;
				}
				return storage;
			}
		}

		storage.valid = true;

		storage.handler.deserializeNBT(nbt);
		return storage;
	}

	public boolean isValid() {
		return valid;
	}

}
