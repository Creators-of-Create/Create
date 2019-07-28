package com.simibubi.create.foundation.utility;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.SlabType;

public class BlockHelper {

	public static int findAndRemoveInInventory(BlockState block, PlayerEntity player, int amount) {
		int amountFound = 0;
		Item required = getRequiredItem(block).getItem();

		boolean needsTwo = block.has(BlockStateProperties.SLAB_TYPE)
				&& block.get(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE;

		if (needsTwo)
			amount *= 2;

		{
			// Try held Item first
			int preferredSlot = player.inventory.currentItem;
			ItemStack itemstack = player.inventory.getStackInSlot(preferredSlot);
			int count = itemstack.getCount();
			if (itemstack.getItem() == required && count > 0) {
				int taken = Math.min(count, amount - amountFound);
				player.inventory.setInventorySlotContents(preferredSlot,
						new ItemStack(itemstack.getItem(), count - taken));
				amountFound += taken;
			}
		}

		// Search inventory
		for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
			if (amountFound == amount)
				break;

			ItemStack itemstack = player.inventory.getStackInSlot(i);
			int count = itemstack.getCount();
			if (itemstack.getItem() == required && count > 0) {
				int taken = Math.min(count, amount - amountFound);
				player.inventory.setInventorySlotContents(i, new ItemStack(itemstack.getItem(), count - taken));
				amountFound += taken;
			}
		}

		if (needsTwo) {
			// Give back 1 if uneven amount was removed
			if (amountFound % 2 != 0)
				player.inventory.addItemStackToInventory(new ItemStack(required));
			amountFound /= 2;
		}

		return amountFound;
	}

	public static ItemStack getRequiredItem(BlockState state) {
		ItemStack itemStack = new ItemStack(state.getBlock());
		return itemStack;
	}

}
