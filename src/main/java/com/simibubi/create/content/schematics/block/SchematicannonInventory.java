package com.simibubi.create.content.schematics.block;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.ItemStackHandler;

public class SchematicannonInventory extends ItemStackHandler {
	private final SchematicannonBlockEntity blockEntity;

	public SchematicannonInventory(SchematicannonBlockEntity blockEntity) {
		super(5);
		this.blockEntity = blockEntity;
	}

	@Override
	protected void onContentsChanged(int slot) {
		super.onContentsChanged(slot);
		blockEntity.setChanged();
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		switch (slot) {
		case 0: // Blueprint Slot
			return AllItems.SCHEMATIC.isIn(stack);
		case 1: // Blueprint output
			return false;
		case 2: // Book input
			return AllBlocks.CLIPBOARD.isIn(stack) || stack.sameItem(new ItemStack(Items.BOOK))
				|| stack.sameItem(new ItemStack(Items.WRITTEN_BOOK));
		case 3: // Material List output
			return false;
		case 4: // Gunpowder
			return stack.sameItem(new ItemStack(Items.GUNPOWDER));
		default:
			return super.isItemValid(slot, stack);
		}
	}
}