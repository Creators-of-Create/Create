package com.simibubi.create.content.itemprocessing.specifics;

/**
 * Models the specific ways an item can be processed
 * This is for example on a belt, in world, on a basin etc.
 */
public interface ProcessingSpecifics {

	/**
	 * @return whether this machine can bulk process items. Bulk in this context means multiple items at once
	 */
	boolean canProcessInBulk();

	void onFinished();
}
