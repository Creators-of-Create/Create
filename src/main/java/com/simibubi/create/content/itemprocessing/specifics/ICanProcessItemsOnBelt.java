package com.simibubi.create.content.itemprocessing.specifics;

import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface ICanProcessItemsOnBelt {

	/**
	 * Tries to process the item on a belt
	 * @param input the item on the belt
	 * @param simulate whether this only simulates the process
	 * @return whether the process was successful or not
	 */
	boolean tryProcessOnBelt(TransportedItemStack input, List<ItemStack> outputList, boolean simulate);

}
