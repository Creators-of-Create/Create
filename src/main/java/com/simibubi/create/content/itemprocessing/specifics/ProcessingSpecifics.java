package com.simibubi.create.content.itemprocessing.specifics;

import com.simibubi.create.content.processing.ProcessingMode;

/**
 * Models the specific ways an item can be processed
 * This is for example on a belt, in world, on a basin etc.
 * @see ICanProcessInBasin
 * @see ICanProcessInWorldItems
 * @see ICanProcessInWorldItems
 */
public interface ProcessingSpecifics {

	/**
	 * @return whether this machine can bulk process items. Bulk in this context means multiple items at once
	 */
	boolean canProcessInBulk();

	/**
	 * called when the processing has started
	 * @param mode the processing mode
	 */
	default void onStart(ProcessingMode mode) {};

	/**
	 * called when the processing has finished
	 */
	default void onFinished() {};
}
