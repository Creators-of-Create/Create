package com.simibubi.create.content.equipment.toolbox;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.contraption.storage.item.WrapperMountedItemStorage;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.foundation.item.ItemHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public class ToolboxMountedStorage extends WrapperMountedItemStorage<ToolboxInventory> {
	public static final MapCodec<ToolboxMountedStorage> CODEC = ToolboxInventory.CODEC.xmap(
		ToolboxMountedStorage::new, storage -> storage.wrapped
	).fieldOf("value");

	protected ToolboxMountedStorage(MountedItemStorageType<?> type, ToolboxInventory wrapped) {
		super(type, wrapped);
	}

	protected ToolboxMountedStorage(ToolboxInventory wrapped) {
		this(AllMountedStorageTypes.TOOLBOX.get(), wrapped);
	}

	@Override
	public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		if (be instanceof ToolboxBlockEntity toolbox) {
			ItemHelper.copyContents(this, toolbox.inventory);
		}
	}

	@Override
	public boolean handleInteraction(ServerPlayer player, Contraption contraption, StructureBlockInfo info) {
		// The default impl will fail anyway, might as well cancel trying
		return false;
	}

	public static ToolboxMountedStorage fromToolbox(ToolboxBlockEntity toolbox) {
		// the inventory will send updates to the block entity, make an isolated copy to avoid that
		ToolboxInventory copy = new ToolboxInventory(null);
		ItemHelper.copyContents(toolbox.inventory, copy);

		copy.filters.clear();
		for (ItemStack stack : toolbox.inventory.filters)
			copy.filters.add(stack.copy());

		return new ToolboxMountedStorage(copy);
	}

	public static ToolboxMountedStorage fromLegacy(HolderLookup.Provider registries, CompoundTag nbt) {
		ToolboxInventory inv = new ToolboxInventory(null);
		inv.deserializeNBT(registries, nbt);
		return new ToolboxMountedStorage(inv);
	}
}
