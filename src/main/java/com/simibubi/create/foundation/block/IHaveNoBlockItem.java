package com.simibubi.create.foundation.block;

/**
 * Blocks only registered for use outside of the inventory
 */
public interface IHaveNoBlockItem {

	default boolean hasBlockItem() {
		return false;
	}
	
}
