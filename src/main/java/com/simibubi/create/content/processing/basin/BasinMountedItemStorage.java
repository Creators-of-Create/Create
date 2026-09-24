package com.simibubi.create.content.processing.basin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.api.contraption.storage.SyncedMountedStorage;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorage;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.foundation.codec.CreateCodecs;

import com.simibubi.create.foundation.utility.InventoryUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import net.neoforged.neoforge.items.IItemHandlerModifiable;

import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BasinMountedItemStorage extends MountedItemStorage implements SyncedMountedStorage {
	public static final MapCodec<BasinMountedItemStorage> CODEC = RecordCodecBuilder.mapCodec(
		i -> i.group(
			CreateCodecs.BASIN_INVENTORY.fieldOf("inputInventory").forGetter(BasinMountedItemStorage::getInputInventory),
			CreateCodecs.BASIN_INVENTORY.fieldOf("outputInventory").forGetter(BasinMountedItemStorage::getOutputInventory)
		).apply(i, BasinMountedItemStorage::fromCodec)
	);

	protected BasinInventory inputInventory;
	protected BasinInventory outputInventory;
	protected IItemHandlerModifiable itemCapability;

	private int timesChanged;
	private boolean dirty;

	protected BasinMountedItemStorage(MountedItemStorageType<?> type) {
		super(type);

		inputInventory = new BasinInventory(9, null);

		outputInventory = new BasinInventory(9, null);
		outputInventory.forbidInsertion().withMaxStackSize(64);

		itemCapability = new CombinedInvWrapper(inputInventory, outputInventory);

		timesChanged = 0;
	}

	protected BasinMountedItemStorage() { this(AllMountedStorageTypes.BASIN_ITEM.get()); }

	@Override
	public boolean handleInteraction(ServerPlayer player, Contraption contraption, StructureBlockInfo info) {
		// interaction is handled in the Interaction Behavior, takes items out from basin
		return false;
	}

	protected static BasinMountedItemStorage fromCodec(BasinInventory input, BasinInventory output) {
		BasinMountedItemStorage basinMountedItemStorage = new BasinMountedItemStorage();

		InventoryUtil.copyInventoryToFrom(basinMountedItemStorage.inputInventory, input);
		InventoryUtil.copyInventoryToFrom(basinMountedItemStorage.outputInventory, output);

		basinMountedItemStorage.listenForChanges();

		return basinMountedItemStorage;
	}

	public static BasinMountedItemStorage fromBasin(BasinBlockEntity basin) {
		BasinMountedItemStorage basinMountedItemStorage = new BasinMountedItemStorage();

		InventoryUtil.copyInventoryToFrom(basinMountedItemStorage.inputInventory, basin.inputInventory);
		InventoryUtil.copyInventoryToFrom(basinMountedItemStorage.outputInventory, basin.outputInventory);

		basinMountedItemStorage.listenForChanges();

		return basinMountedItemStorage;
	}

	protected void listenForChanges() {
		this.inputInventory.whenContentsChanged(this::increaseTimesChanged);
		this.outputInventory.whenContentsChanged(this::increaseTimesChanged);
	}

	public BasinInventory getInputInventory() { return this.inputInventory; }
	public BasinInventory getOutputInventory() { return this.outputInventory; }
	public IItemHandlerModifiable getCapability() { return this.itemCapability; }

	public int getTimesChanged() { return timesChanged; }

	@Override
	public boolean isDirty() { return this.dirty; }

	@Override
	public void markClean() { this.dirty = false; }

	public void increaseTimesChanged(int $) {
		this.timesChanged++;
		this.dirty = true;
	}

	@Override
	public void afterSync(Contraption contraption, BlockPos localPos) {}

	@Override
	public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		if(be instanceof BasinBlockEntity basin) {
			basin.applyInventories(inputInventory, outputInventory);
		}
	}

	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack itemStack) {
		itemCapability.setStackInSlot(slot, itemStack);
	}

	@Override
	public int getSlots() {
		return itemCapability.getSlots();
	}

	@Override
	@NotNull
	public ItemStack getStackInSlot(int slot) {
		return itemCapability.getStackInSlot(slot);
	}

	@Override
	@NotNull
	public ItemStack insertItem(int slot, @NotNull ItemStack itemStack, boolean simulate) {
		return itemCapability.insertItem(slot, itemStack, simulate);
	}

	@Override
	@NotNull
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return itemCapability.extractItem(slot, amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot) {
		return itemCapability.getSlotLimit(slot);
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack itemStack) {
		return itemCapability.isItemValid(slot, itemStack);
	}
}
