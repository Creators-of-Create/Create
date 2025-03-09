package com.simibubi.create.content.processing;

import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;

/**
 * Models a callback function which is called when a certain step of a process is reached
 */
@FunctionalInterface
public interface ProcessingCallback {
	/**
	 *
	 * @param stack which is being processed
	 * @param inventory TODO find out what this does :)
	 * @return state of process after callback
	 */
	ProcessingResult apply(TransportedItemStack stack, TransportedItemStackHandlerBehaviour inventory);
}
