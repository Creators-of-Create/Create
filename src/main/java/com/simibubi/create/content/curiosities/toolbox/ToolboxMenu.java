package com.simibubi.create.content.curiosities.toolbox;

import static com.simibubi.create.content.curiosities.toolbox.ToolboxInventory.STACKS_PER_COMPARTMENT;

import com.simibubi.create.AllMenuTypes;
import com.simibubi.create.foundation.gui.menu.MenuBase;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class ToolboxMenu extends MenuBase<ToolboxBlockEntity> {

	public ToolboxMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
		super(type, id, inv, extraData);
	}

	public ToolboxMenu(MenuType<?> type, int id, Inventory inv, ToolboxBlockEntity be) {
		super(type, id, inv, be);
		be.startOpen(player);
	}

	public static ToolboxMenu create(int id, Inventory inv, ToolboxBlockEntity be) {
		return new ToolboxMenu(AllMenuTypes.TOOLBOX.get(), id, inv, be);
	}

	@Override
	protected ToolboxBlockEntity createOnClient(FriendlyByteBuf extraData) {
		BlockPos readBlockPos = extraData.readBlockPos();
		CompoundTag readNbt = extraData.readNbt();

		ClientLevel world = Minecraft.getInstance().level;
		BlockEntity blockEntity = world.getBlockEntity(readBlockPos);
		if (blockEntity instanceof ToolboxBlockEntity) {
			ToolboxBlockEntity toolbox = (ToolboxBlockEntity) blockEntity;
			toolbox.readClient(readNbt);
			return toolbox;
		}

		return null;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot clickedSlot = getSlot(index);
		if (!clickedSlot.hasItem())
			return ItemStack.EMPTY;

		ItemStack stack = clickedSlot.getItem();
		int size = contentHolder.inventory.getSlots();
		boolean success = false;
		if (index < size) {
			success = !moveItemStackTo(stack, size, slots.size(), false);
			contentHolder.inventory.onContentsChanged(index);
		} else
			success = !moveItemStackTo(stack, 0, size - 1, false);

		return success ? ItemStack.EMPTY : stack;
	}

	@Override
	protected void initAndReadInventory(ToolboxBlockEntity contentHolder) {

	}

	@Override
	public void clicked(int index, int flags, ClickType type, Player player) {
		int size = contentHolder.inventory.getSlots();

		if (index >= 0 && index < size) {
			ItemStack itemInClickedSlot = getSlot(index).getItem();
			ItemStack carried = getCarried();

			if (type == ClickType.PICKUP && !carried.isEmpty() && !itemInClickedSlot.isEmpty()
				&& ToolboxInventory.canItemsShareCompartment(itemInClickedSlot, carried)) {
				int subIndex = index % STACKS_PER_COMPARTMENT;
				if (subIndex != STACKS_PER_COMPARTMENT - 1) {
					clicked(index - subIndex + STACKS_PER_COMPARTMENT - 1, flags, type, player);
					return;
				}
			}

			if (type == ClickType.PICKUP && carried.isEmpty() && itemInClickedSlot.isEmpty())
				if (!player.level.isClientSide) {
					contentHolder.inventory.filters.set(index / STACKS_PER_COMPARTMENT, ItemStack.EMPTY);
					contentHolder.sendData();
				}

		}
		super.clicked(index, flags, type, player);
	}

	@Override
	public boolean canDragTo(Slot slot) {
		return slot.index > contentHolder.inventory.getSlots() && super.canDragTo(slot);
	}

	public ItemStack getFilter(int compartment) {
		return contentHolder.inventory.filters.get(compartment);
	}

	public int totalCountInCompartment(int compartment) {
		int count = 0;
		int baseSlot = compartment * STACKS_PER_COMPARTMENT;
		for (int i = 0; i < STACKS_PER_COMPARTMENT; i++)
			count += getSlot(baseSlot + i).getItem()
				.getCount();
		return count;
	}

	public boolean renderPass;

	@Override
	protected void addSlots() {
		ToolboxInventory inventory = contentHolder.inventory;

		int x = 79;
		int y = 37;

		int[] xOffsets = { x, x + 33, x + 66, x + 66 + 6, x + 66, x + 33, x, x - 6 };
		int[] yOffsets = { y, y - 6, y, y + 33, y + 66, y + 66 + 6, y + 66, y + 33 };

		for (int compartment = 0; compartment < 8; compartment++) {
			int baseIndex = compartment * STACKS_PER_COMPARTMENT;

			// Representative Slots
			addSlot(new ToolboxSlot(this, inventory, baseIndex, xOffsets[compartment], yOffsets[compartment]));

			// Hidden Slots
			for (int i = 1; i < STACKS_PER_COMPARTMENT; i++)
				addSlot(new SlotItemHandler(inventory, baseIndex + i, -10000, -10000));
		}

		addPlayerSlots(8, 165);
	}

	@Override
	protected void saveData(ToolboxBlockEntity contentHolder) {

	}

	@Override
	public void removed(Player playerIn) {
		super.removed(playerIn);
		if (!playerIn.level.isClientSide)
			contentHolder.stopOpen(playerIn);
	}

}
