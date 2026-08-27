package com.simibubi.create.foundation.utility;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class InventoryUtil {
	public static void copyInventoryToFrom(IItemHandlerModifiable copy, IItemHandler original) {
		for (int i = 0; i < original.getSlots(); i++) {
			copy.setStackInSlot(i, original.getStackInSlot(i).copy());
		}
	}
}
