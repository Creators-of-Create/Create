package com.simibubi.create.modules.contraptions.components.crafter;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.modules.contraptions.components.crafter.RecipeGridHandler.GroupedItems;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;

public class MechanicalCraftingInventory extends CraftingInventory {

	private static Container dummyContainer = new Container(null, -1) {
		public boolean canInteractWith(PlayerEntity playerIn) {
			return false;
		}
	};

	public MechanicalCraftingInventory(GroupedItems items) {
		super(dummyContainer, items.width, items.height);
		for (int y = 0; y < items.height; y++) {
			for (int x = 0; x < items.width; x++) {
				ItemStack stack = items.grid.get(Pair.of(x + items.minX, y + items.minY));
				setInventorySlotContents(x + (items.height - y - 1) * items.width,
						stack == null ? ItemStack.EMPTY : stack.copy());
			}
		}
	}

}
