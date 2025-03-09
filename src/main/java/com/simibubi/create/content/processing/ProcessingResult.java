package com.simibubi.create.content.processing;


/**
 * Models the current result of an item process
 * @see ProcessingBehaviour
 */
public enum ProcessingResult {
	/**
	 * Machine which will process the item can process it and pass it forward
	 */
	PASS,

	/**
	 * Machine can't process the item (for example spout is missing fluid), therefore the item is on hold
	 */
	HOLD,

	/**
	 * Remove the item from the current process (throw it off belt)
	 */
	REMOVE;
}
