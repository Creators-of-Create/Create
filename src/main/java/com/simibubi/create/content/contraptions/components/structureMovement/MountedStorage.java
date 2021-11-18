package com.simibubi.create.content.contraptions.components.structureMovement;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.processing.ProcessingInventory;
import com.simibubi.create.content.logistics.block.inventories.AdjustableCrateBlock;
import com.simibubi.create.content.logistics.block.inventories.BottomlessItemHandler;
import com.simibubi.create.foundation.utility.NBTHelper;

import com.simibubi.create.lib.transfer.TransferUtil;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;

import com.simibubi.create.lib.transfer.item.IItemHandler;
import com.simibubi.create.lib.transfer.item.IItemHandlerModifiable;
import com.simibubi.create.lib.transfer.item.ItemStackHandler;
import com.simibubi.create.lib.utility.LazyOptional;
import com.simibubi.create.lib.utility.NBTSerializer;

public class MountedStorage {

	private static final ItemStackHandler dummyHandler = new ItemStackHandler();

	ItemStackHandler handler;
	boolean valid;
	private BlockEntity te;

	public static boolean canUseAsStorage(BlockEntity te) {
		if (te == null)
			return false;

		if (AllTileEntities.ADJUSTABLE_CRATE.is(te))
			return true;
		if (AllTileEntities.CREATIVE_CRATE.is(te))
			return true;
		if (te instanceof ShulkerBoxBlockEntity)
			return true;
		if (te instanceof ChestBlockEntity)
			return true;
		if (te instanceof BarrelBlockEntity)
			return true;

		LazyOptional<IItemHandler> capability = TransferUtil.getItemHandler(te);
		IItemHandler handler = capability.orElse(null);
		return handler instanceof ItemStackHandler && !(handler instanceof ProcessingInventory);
	}

	public MountedStorage(BlockEntity te) {
		this.te = te;
		handler = dummyHandler;
	}

	public void removeStorageFromWorld() {
		valid = false;
		if (te == null)
			return;

		// Split double chests
		if (te.getType() == BlockEntityType.CHEST || te.getType() == BlockEntityType.TRAPPED_CHEST) {
			if (te.getBlockState()
				.getValue(ChestBlock.TYPE) != ChestType.SINGLE)
				te.getLevel()
					.setBlockAndUpdate(te.getBlockPos(), te.getBlockState()
						.setValue(ChestBlock.TYPE, ChestType.SINGLE));
		}

		// Split double flexcrates
		if (AllTileEntities.ADJUSTABLE_CRATE.is(te)) {
			if (te.getBlockState()
				.getValue(AdjustableCrateBlock.DOUBLE))
				te.getLevel()
					.setBlockAndUpdate(te.getBlockPos(), te.getBlockState()
						.setValue(AdjustableCrateBlock.DOUBLE, false));
		}

		IItemHandler teHandler = TransferUtil.getItemHandler(te)
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

	public void addStorageToWorld(BlockEntity te) {
		// FIXME: More dynamic mounted storage in .4
		if (handler instanceof BottomlessItemHandler)
			return;

		LazyOptional<IItemHandler> capability = TransferUtil.getItemHandler(te);
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

		if (handler instanceof BottomlessItemHandler) {
			NBTHelper.putMarker(tag, "Bottomless");
			tag.put("ProvidedStack", NBTSerializer.serializeNBT(handler.getStackInSlot(0)));
		}

		return tag;
	}

	public static MountedStorage deserialize(CompoundTag nbt) {
		MountedStorage storage = new MountedStorage(null);
		storage.handler = new ItemStackHandler();
		if (nbt == null)
			return storage;
		storage.valid = true;

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

}
