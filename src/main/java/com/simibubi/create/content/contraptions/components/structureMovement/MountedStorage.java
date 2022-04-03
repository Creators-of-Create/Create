package com.simibubi.create.content.contraptions.components.structureMovement;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterTileEntity;
import com.simibubi.create.content.contraptions.processing.ProcessingInventory;
import com.simibubi.create.content.logistics.block.inventories.BottomlessItemHandler;
import com.simibubi.create.content.logistics.block.vault.ItemVaultTileEntity;
import com.simibubi.create.foundation.utility.NBTHelper;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.util.NBTSerializer;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

import java.util.List;

public class MountedStorage {

	private static final ItemStackHandler dummyHandler = new ItemStackHandler();

	ItemStackHandler handler;
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

		Storage<ItemVariant> handler = TransferUtil.getItemStorage(te);
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

		if (te instanceof ChestBlockEntity) {
			CompoundTag tag = te.saveWithFullMetadata();
			if (tag.contains("LootTable", 8))
				return;

			handler = new ItemStackHandler(((ChestBlockEntity) te).getContainerSize());
			NonNullList<ItemStack> items = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
			ContainerHelper.loadAllItems(tag, items);
			handler.stacks = items.toArray(ItemStack[]::new);
			valid = true;
			return;
		}

		Storage<ItemVariant> teHandler = TransferUtil.getItemStorage(te);
		if (teHandler == null)
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
		if (teHandler.supportsExtraction() && teHandler.supportsInsertion()) {
			try (Transaction t = TransferUtil.getTransaction()) {
				List<ItemStack> stacks = TransferUtil.getAllItems(teHandler);
				if (TransferUtil.clearStorage(teHandler)) {
					handler = new ItemStackHandler(stacks.toArray(ItemStack[]::new));
					t.commit();
					valid = true;
					return;
				}
			}
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

		Storage<ItemVariant> teHandler = TransferUtil.getItemStorage(te);
		StorageUtil.move(handler, teHandler, v -> true, Long.MAX_VALUE, null);
	}

	public Storage<ItemVariant> getItemHandler() {
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
