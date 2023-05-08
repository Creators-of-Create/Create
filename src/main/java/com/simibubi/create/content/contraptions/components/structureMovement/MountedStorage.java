package com.simibubi.create.content.contraptions.components.structureMovement;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterBlockEntity;
import com.simibubi.create.content.contraptions.processing.ProcessingInventory;
import com.simibubi.create.content.logistics.block.inventories.BottomlessItemHandler;
import com.simibubi.create.content.logistics.block.vault.ItemVaultBlockEntity;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;

public class MountedStorage {

	private static final ItemStackHandler dummyHandler = new ItemStackHandler();

	ItemStackHandler handler;
	boolean noFuel;
	boolean valid;

	private BlockEntity blockEntity;

	public static boolean canUseAsStorage(BlockEntity be) {
		if (be == null)
			return false;
		if (be instanceof MechanicalCrafterBlockEntity)
			return false;
		if (AllBlockEntityTypes.CREATIVE_CRATE.is(be))
			return true;
		if (be instanceof ShulkerBoxBlockEntity)
			return true;
		if (be instanceof ChestBlockEntity)
			return true;
		if (be instanceof BarrelBlockEntity)
			return true;
		if (be instanceof ItemVaultBlockEntity)
			return true;

		try {
			LazyOptional<IItemHandler> capability = be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
			IItemHandler handler = capability.orElse(null);
			if (handler instanceof ItemStackHandler)
				return !(handler instanceof ProcessingInventory);
			return canUseModdedInventory(be, handler);
			
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean canUseModdedInventory(BlockEntity be, IItemHandler handler) {
		if (!(handler instanceof IItemHandlerModifiable validItemHandler))
			return false;
		BlockState blockState = be.getBlockState();
		if (AllBlockTags.CONTRAPTION_INVENTORY_DENY.matches(blockState))
			return false;

		// There doesn't appear to be much of a standard for tagging chests/barrels
		String blockId = ForgeRegistries.BLOCKS.getKey(blockState.getBlock())
			.getPath();
		return blockId.endsWith("_chest") || blockId.endsWith("_barrel");
	}

	public MountedStorage(BlockEntity be) {
		this.blockEntity = be;
		handler = dummyHandler;
		noFuel = be instanceof ItemVaultBlockEntity;
	}

	public void removeStorageFromWorld() {
		valid = false;
		if (blockEntity == null)
			return;

		if (blockEntity instanceof ChestBlockEntity) {
			CompoundTag tag = blockEntity.saveWithFullMetadata();
			if (tag.contains("LootTable", 8))
				return;

			handler = new ItemStackHandler(((ChestBlockEntity) blockEntity).getContainerSize());
			NonNullList<ItemStack> items = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
			ContainerHelper.loadAllItems(tag, items);
			for (int i = 0; i < items.size(); i++)
				handler.setStackInSlot(i, items.get(i));
			valid = true;
			return;
		}

		IItemHandler beHandler = blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			.orElse(dummyHandler);
		if (beHandler == dummyHandler)
			return;

		// multiblock vaults need to provide individual invs
		if (blockEntity instanceof ItemVaultBlockEntity) {
			handler = ((ItemVaultBlockEntity) blockEntity).getInventoryOfBlock();
			valid = true;
			return;
		}

		// be uses ItemStackHandler
		if (beHandler instanceof ItemStackHandler) {
			handler = (ItemStackHandler) beHandler;
			valid = true;
			return;
		}

		// serialization not accessible -> fill into a serializable handler
		if (beHandler instanceof IItemHandlerModifiable) {
			IItemHandlerModifiable inv = (IItemHandlerModifiable) beHandler;
			handler = new ItemStackHandler(beHandler.getSlots());
			for (int slot = 0; slot < handler.getSlots(); slot++) {
				handler.setStackInSlot(slot, inv.getStackInSlot(slot));
				inv.setStackInSlot(slot, ItemStack.EMPTY);
			}
			valid = true;
			return;
		}

	}

	public void addStorageToWorld(BlockEntity be) {
		// FIXME: More dynamic mounted storage in .4
		if (handler instanceof BottomlessItemHandler)
			return;

		if (be instanceof ChestBlockEntity) {
			CompoundTag tag = be.saveWithFullMetadata();
			tag.remove("Items");
			NonNullList<ItemStack> items = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
			for (int i = 0; i < items.size(); i++)
				items.set(i, handler.getStackInSlot(i));
			ContainerHelper.saveAllItems(tag, items);
			be.load(tag);
			return;
		}

		if (be instanceof ItemVaultBlockEntity) {
			((ItemVaultBlockEntity) be).applyInventoryToBlock(handler);
			return;
		}

		LazyOptional<IItemHandler> capability = be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
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
