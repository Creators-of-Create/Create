package com.simibubi.create.foundation.tileEntity.behaviour.belt;

import com.simibubi.create.content.contraptions.relays.belt.transport.BeltInventory;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;

/**
 * Behaviour for TileEntities which can process items on belts or depots beneath them.
 * Currently only supports placement location 2 spaces above the belt block.
 * Example use: Mechanical Press
 */
public class BeltProcessingBehaviour extends TileEntityBehaviour {

	public static BehaviourType<BeltProcessingBehaviour> TYPE = new BehaviourType<>();

	public static enum ProcessingResult {
		PASS, HOLD, REMOVE;
	}
	
	private ProcessingCallback onItemEnter;
	private ProcessingCallback continueProcessing;

	public BeltProcessingBehaviour(SmartTileEntity te) {
		super(te);
		onItemEnter = (s, i) -> ProcessingResult.PASS;
		continueProcessing = (s, i) -> ProcessingResult.PASS;
	}
	
	public BeltProcessingBehaviour whenItemEnters(ProcessingCallback callback) {
		onItemEnter = callback;
		return this;
	}
	
	public BeltProcessingBehaviour whileItemHeld(ProcessingCallback callback) {
		continueProcessing = callback;
		return this;
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	public ProcessingResult handleReceivedItem(TransportedItemStack stack, BeltInventory inventory) {
		return onItemEnter.apply(stack, inventory);
	}

	public ProcessingResult handleHeldItem(TransportedItemStack stack, BeltInventory inventory) {
		return continueProcessing.apply(stack, inventory);
	}

	@FunctionalInterface
	public interface ProcessingCallback {
		public ProcessingResult apply(TransportedItemStack stack, BeltInventory inventory);
	}

}
