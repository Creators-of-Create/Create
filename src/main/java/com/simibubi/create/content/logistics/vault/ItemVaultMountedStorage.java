package com.simibubi.create.content.logistics.vault;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.contraption.storage.item.WrapperMountedItemStorage;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.foundation.codec.CreateCodecs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import net.neoforged.neoforge.items.ItemStackHandler;

public class ItemVaultMountedStorage extends WrapperMountedItemStorage<ItemStackHandler> {
	public static final MapCodec<ItemVaultMountedStorage> CODEC = CreateCodecs.ITEM_STACK_HANDLER.xmap(
		ItemVaultMountedStorage::new, storage -> storage.wrapped
	).fieldOf("value");

	protected ItemVaultMountedStorage(MountedItemStorageType<?> type, ItemStackHandler handler) {
		super(type, handler);
	}

	protected ItemVaultMountedStorage(ItemStackHandler handler) {
		this(AllMountedStorageTypes.VAULT.get(), handler);
	}

	@Override
	public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		if (be instanceof ItemVaultBlockEntity vault) {
			vault.applyInventoryToBlock(this.wrapped);
		}
	}

	@Override
	public boolean handleInteraction(ServerPlayer player, Contraption contraption, StructureBlockInfo info) {
		// vaults should never be opened.
		return false;
	}

	public static ItemVaultMountedStorage fromVault(ItemVaultBlockEntity vault) {
		// Vault inventories have a world-affecting onContentsChanged, copy to a safe one
		return new ItemVaultMountedStorage(copyToItemStackHandler(vault.getInventoryOfBlock()));
	}

	public static ItemVaultMountedStorage fromLegacy(HolderLookup.Provider registries,  CompoundTag nbt) {
		ItemStackHandler handler = new ItemStackHandler();
		handler.deserializeNBT(registries, nbt);
		return new ItemVaultMountedStorage(handler);
	}
}
