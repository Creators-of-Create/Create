package com.simibubi.create.content.equipment.toolbox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.ItemSlots;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import net.neoforged.neoforge.items.ItemStackHandler;

public class ToolboxInventory extends ItemStackHandler {
	public static final int STACKS_PER_COMPARTMENT = 4;
	public static final Codec<ToolboxInventory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ItemSlots.maxSizeCodec(8 * STACKS_PER_COMPARTMENT).fieldOf("items").forGetter(ItemSlots::fromHandler),
		ItemStack.OPTIONAL_CODEC.listOf().fieldOf("filters").forGetter(toolbox -> toolbox.filters)
	).apply(instance, ToolboxInventory::deserialize));

	public static final StreamCodec<RegistryFriendlyByteBuf, ToolboxInventory> STREAM_CODEC = StreamCodec.composite(
		ItemSlots.STREAM_CODEC, ItemSlots::fromHandler,
		CatnipStreamCodecBuilders.list(ItemStack.OPTIONAL_STREAM_CODEC), toolbox -> toolbox.filters,
		ToolboxInventory::deserialize
	);

	@ScheduledForRemoval(inVersion = "1.21.1+ Port")
	@Deprecated(since = "6.0.6", forRemoval = true)
	public static final Codec<ToolboxInventory> BACKWARDS_COMPAT_CODEC = Codec.withAlternative(
		CODEC,
		ItemContainerContents.CODEC.xmap(i -> {
			ToolboxInventory inv = new ToolboxInventory(null);
			ItemHelper.fillItemStackHandler(i, inv);
			return inv;
		}, ItemHelper::containerContentsFromHandler)
	);

	List<ItemStack> filters;
	boolean settling;
	private final ToolboxBlockEntity blockEntity;

	private boolean limitedMode;

	public ToolboxInventory(ToolboxBlockEntity be) {
		super(8 * STACKS_PER_COMPARTMENT);
		this.blockEntity = be;
		limitedMode = false;
		filters = new ArrayList<>();
		settling = false;
		for (int i = 0; i < 8; i++)
			filters.add(ItemStack.EMPTY);
	}

	public void inLimitedMode(Consumer<ToolboxInventory> action) {
		limitedMode = true;
		action.accept(this);
		limitedMode = false;
	}

	public void settle(int compartment) {
		int totalCount = 0;
		boolean valid = true;
		boolean shouldBeEmpty = false;
		ItemStack sample = ItemStack.EMPTY;

		for (int i = 0; i < STACKS_PER_COMPARTMENT; i++) {
			ItemStack stackInSlot = getStackInSlot(compartment * STACKS_PER_COMPARTMENT + i);
			totalCount += stackInSlot.getCount();
			if (!shouldBeEmpty)
				shouldBeEmpty = stackInSlot.isEmpty() || stackInSlot.getCount() != stackInSlot.getMaxStackSize();
			else if (!stackInSlot.isEmpty()) {
				valid = false;
				sample = stackInSlot;
			}
		}

		if (valid)
			return;

		settling = true;
		if (!sample.isStackable()) {
			for (int i = 0; i < STACKS_PER_COMPARTMENT; i++) {
				if (!getStackInSlot(compartment * STACKS_PER_COMPARTMENT + i).isEmpty())
					continue;
				for (int j = i + 1; j < STACKS_PER_COMPARTMENT; j++) {
					ItemStack stackInSlot = getStackInSlot(compartment * STACKS_PER_COMPARTMENT + j);
					if (stackInSlot.isEmpty())
						continue;
					setStackInSlot(compartment * STACKS_PER_COMPARTMENT + i, stackInSlot);
					setStackInSlot(compartment * STACKS_PER_COMPARTMENT + j, ItemStack.EMPTY);
					break;
				}
			}
		} else {
			for (int i = 0; i < STACKS_PER_COMPARTMENT; i++) {
				ItemStack copy = totalCount <= 0 ? ItemStack.EMPTY
					: sample.copyWithCount(Math.min(totalCount, sample.getMaxStackSize()));
				setStackInSlot(compartment * STACKS_PER_COMPARTMENT + i, copy);
				totalCount -= copy.getCount();
			}
		}
		settling = false;
		notifyUpdate();
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		if (!stack.getItem().canFitInsideContainerItems())
			return false;

		if (slot < 0 || slot >= getSlots())
			return false;
		int compartment = slot / STACKS_PER_COMPARTMENT;
		ItemStack filter = filters.get(compartment);
		if (limitedMode && filter.isEmpty())
			return false;
		if (filter.isEmpty() || ToolboxInventory.canItemsShareCompartment(filter, stack))
			return super.isItemValid(slot, stack);
		return false;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		super.setStackInSlot(slot, stack);
		int compartment = slot / STACKS_PER_COMPARTMENT;
		if (!stack.isEmpty() && filters.get(compartment)
			.isEmpty()) {
			filters.set(compartment, stack.copyWithCount(1));
			notifyUpdate();
		}
	}

	@Override
	public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		ItemStack insertItem = super.insertItem(slot, stack, simulate);
		if (insertItem.getCount() != stack.getCount()) {
			int compartment = slot / STACKS_PER_COMPARTMENT;
			if (!stack.isEmpty() && filters.get(compartment)
				.isEmpty()) {
				filters.set(compartment, stack.copyWithCount(1));
				notifyUpdate();
			}
		}
		return insertItem;
	}

	@Override
	public @NotNull CompoundTag serializeNBT(@NotNull HolderLookup.Provider registries) {
		CompoundTag compound = super.serializeNBT(registries);
		compound.put("Compartments", NBTHelper.writeItemList(filters, registries));
		return compound;
	}

	@Override
	protected void onContentsChanged(int slot) {
		if (!settling && (blockEntity == null || !blockEntity.getLevel().isClientSide))
			settle(slot / STACKS_PER_COMPARTMENT);
		notifyUpdate();
		super.onContentsChanged(slot);
	}

	@Override
	public void deserializeNBT(@NotNull HolderLookup.Provider registries, CompoundTag nbt) {
		filters = NBTHelper.readItemList(nbt.getList("Compartments", Tag.TAG_COMPOUND), registries);
		if (filters.size() != 8) {
			filters.clear();
			for (int i = 0; i < 8; i++)
				filters.add(ItemStack.EMPTY);
		}
		super.deserializeNBT(registries, nbt);
	}

	public ItemStack distributeToCompartment(@NotNull ItemStack stack, int compartment, boolean simulate) {
		if (stack.isEmpty())
			return stack;
		if (filters.get(compartment)
			.isEmpty())
			return stack;

		for (int i = STACKS_PER_COMPARTMENT - 1; i >= 0; i--) {
			int slot = compartment * STACKS_PER_COMPARTMENT + i;
			stack = insertItem(slot, stack, simulate);
			if (stack.isEmpty())
				return ItemStack.EMPTY;
		}

		return stack;
	}

	public ItemStack takeFromCompartment(int amount, int compartment, boolean simulate) {
		if (amount == 0)
			return ItemStack.EMPTY;

		int remaining = amount;
		ItemStack lastValid = ItemStack.EMPTY;

		for (int i = STACKS_PER_COMPARTMENT - 1; i >= 0; i--) {
			int slot = compartment * STACKS_PER_COMPARTMENT + i;
			ItemStack extracted = extractItem(slot, remaining, simulate);
			remaining -= extracted.getCount();
			if (!extracted.isEmpty())
				lastValid = extracted;
			if (remaining == 0)
				return lastValid.copyWithCount(amount);
		}

		if (remaining == amount)
			return ItemStack.EMPTY;

		return lastValid.copyWithCount(amount - remaining);
	}

	public static ItemStack cleanItemNBT(ItemStack stack) {
		if (AllItems.BELT_CONNECTOR.isIn(stack))
			stack.remove(AllDataComponents.BELT_FIRST_SHAFT);
		return stack;
	}

	public static boolean canItemsShareCompartment(ItemStack stack1, ItemStack stack2) {
		if (!stack1.isStackable() && !stack2.isStackable() && stack1.isDamageableItem() && stack2.isDamageableItem())
			return stack1.getItem() == stack2.getItem();
		if (AllItems.BELT_CONNECTOR.isIn(stack1) && AllItems.BELT_CONNECTOR.isIn(stack2))
			return true;
		return ItemStack.isSameItemSameComponents(stack1, stack2);
	}

	private void notifyUpdate() {
		if (blockEntity != null)
			blockEntity.notifyUpdate();
	}

	private static ToolboxInventory deserialize(ItemSlots slots, List<ItemStack> filters) {
		ToolboxInventory inventory = new ToolboxInventory(null);
		slots.forEach(inventory::setStackInSlot);
		inventory.filters = new ArrayList<>(filters);
		return inventory;
	}

	@Override
	public final boolean equals(Object o) {
		if (!(o instanceof ToolboxInventory that)) return false;

		return settling == that.settling && limitedMode == that.limitedMode && filters.equals(that.filters)
			&& Objects.equals(blockEntity, that.blockEntity);
	}

	@Override
	public int hashCode() {
		int result = filters.hashCode();
		result = 31 * result + Boolean.hashCode(settling);
		result = 31 * result + Objects.hashCode(blockEntity);
		result = 31 * result + Boolean.hashCode(limitedMode);
		return result;
	}
}
