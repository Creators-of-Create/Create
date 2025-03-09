package com.simibubi.create.content.processing;

import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

/**
 * Models the process of a machine modifying an item.
 * For example mechanical press recipes
 */
abstract public class ProcessingBehaviour extends BlockEntityBehaviour {

	protected ProcessingCallback onItemEnter;
	protected ProcessingCallback continueProcessing;

	public ProcessingBehaviour(SmartBlockEntity be) {
		super(be);
		onItemEnter = (s, i) -> ProcessingResult.PASS;
		continueProcessing = (s, i) -> ProcessingResult.PASS;
	}

	/**
	 * Executed the onItemEnter callback for the given item and
	 * @see ProcessingBehaviour#onItemEnter
	 * @param stack stack to be transformed
	 * @param handler to process item further down machine specific
	 * @return this
	 */
	public ProcessingResult handleReceivedItem(TransportedItemStack stack, TransportedItemStackHandlerBehaviour handler) {
		return onItemEnter.apply(stack, handler);
	}

	/**
	 * Executed the continueItem callback for the given item and
	 * @see ProcessingBehaviour#continueProcessing
	 * @param stack stack to be transformed
	 * @param handler to process item further down machine specific
	 * @return this
	 */
	public ProcessingResult handleHeldItem(TransportedItemStack stack, TransportedItemStackHandlerBehaviour handler) {
		return continueProcessing.apply(stack, handler);
	}

	/**
	 * @return the behavior type. So specific implementation
	 * @see BeltProcessingBehaviour#getType()
	 */
	@Override
	abstract public BehaviourType<?> getType();
}
