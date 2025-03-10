package com.simibubi.create.content.processing.callbacks;

import com.simibubi.create.content.itemprocessing.ICanProcessItems;

import com.simibubi.create.content.itemprocessing.specifics.ProcessingSpecifics;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;

import com.simibubi.create.content.processing.ProcessingResult;


/**
 * Models the callbacks a belt can receive while processing items
 * @param <B> The behaviour which should process the items
 */
public interface IBeltCallbacks<B extends ICanProcessItems<? extends ProcessingSpecifics>> {

	/**
	 * the item has entered the belt block
	 * @param itemStack which entered the belt
	 * @param handler total belt
	 * @param behaviour process behavior of machine
	 * @return ProcessingResult
	 */
	ProcessingResult onItemReceived(TransportedItemStack itemStack, TransportedItemStackHandlerBehaviour handler, B behaviour);

	/**
	 * Called when the item on the belt is stopped and processed
	 * @param itemStack which entered the belt
	 * @param handler total belt
	 * @param behaviour process behavior of machine
	 * @return ProcessingResult
	 */
	ProcessingResult whenItemHeld(TransportedItemStack itemStack, TransportedItemStackHandlerBehaviour handler, B behaviour);


}
