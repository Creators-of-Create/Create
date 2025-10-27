package com.simibubi.create.content.logistics.depot.storage;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.api.contraption.storage.SyncedMountedStorage;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.contraption.storage.item.WrapperMountedItemStorage;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.logistics.depot.storage.DepotMountedStorage.Handler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import net.neoforged.neoforge.items.ItemStackHandler;

public class DepotMountedStorage extends WrapperMountedItemStorage<Handler> implements SyncedMountedStorage {
	public static final MapCodec<DepotMountedStorage> CODEC = ItemStack.OPTIONAL_CODEC.xmap(
		DepotMountedStorage::new, DepotMountedStorage::getItem
	).fieldOf("value");

	private boolean dirty;

	protected DepotMountedStorage(ItemStack stack) {
		this(AllMountedStorageTypes.DEPOT.get(), stack);
	}

	protected DepotMountedStorage(MountedItemStorageType<?> type, ItemStack stack) {
		super(type, new Handler(stack));
		this.wrapped.onChange = () -> this.dirty = true;
	}

	@Override
	public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		if (be instanceof DepotBlockEntity depot) {
			depot.setHeldItem(this.getStackInSlot(0));
		}
	}

	@Override
	public boolean handleInteraction(ServerPlayer player, Contraption contraption, StructureBlockInfo info) {
		// interaction is handled in the Interaction Behavior, swaps items with the player
		return false;
	}

	@Override
	public boolean isDirty() {
		return this.dirty;
	}

	@Override
	public void markClean() {
		this.dirty = false;
	}

	@Override
	public void afterSync(Contraption contraption, BlockPos localPos) {
		BlockEntity be = contraption.getBlockEntityClientSide(localPos);
		if (be instanceof DepotBlockEntity depot) {
			depot.setHeldItem(this.getItem());
		}
	}

	public void setItem(ItemStack stack) {
		this.setStackInSlot(0, stack);
	}

	public ItemStack getItem() {
		return this.getStackInSlot(0);
	}

	public static DepotMountedStorage fromDepot(DepotBlockEntity depot) {
		ItemStack held = depot.getHeldItem();
		return new DepotMountedStorage(held.copy());
	}

	public static DepotMountedStorage fromLegacy(HolderLookup.Provider registries, CompoundTag nbt) {
		ItemStackHandler handler = new ItemStackHandler();
		handler.deserializeNBT(registries, nbt);
		if (handler.getSlots() == 1) {
			ItemStack stack = handler.getStackInSlot(0);
			return new DepotMountedStorage(stack);
		} else {
			return new DepotMountedStorage(ItemStack.EMPTY);
		}
	}

	public static final class Handler extends ItemStackHandler {
		private Runnable onChange = () -> {};

		private Handler(ItemStack stack) {
			super(1);
			this.setStackInSlot(0, stack);
		}

		@Override
		protected void onContentsChanged(int slot) {
			this.onChange.run();
		}
	}
}
