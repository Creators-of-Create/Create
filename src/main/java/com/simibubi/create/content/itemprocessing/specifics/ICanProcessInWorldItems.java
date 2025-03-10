package com.simibubi.create.content.itemprocessing.specifics;

import net.minecraft.world.entity.item.ItemEntity;

public interface ICanProcessInWorldItems {

	/**
	 * Tries to process the item in world, this means the item is dropped in the bounding box of the machine
	 * @param itemEntity item to be processed
	 * @param simulate whether this only simulates the process
	 * @return whether the process was successful or not
	 */
	boolean tryProcessItemInWorld(ItemEntity itemEntity, boolean simulate);

}
