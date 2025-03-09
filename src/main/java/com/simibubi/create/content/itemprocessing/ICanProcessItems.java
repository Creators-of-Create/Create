package com.simibubi.create.content.itemprocessing;


import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

/**
 * Abstraction of machines which can process items
 */
public abstract class ICanProcessItems extends BeltProcessingBehaviour {

	private final int cycle;
	private final int entityScan;

	/**
	 * @param cycle ticks it takes to process (for example mechanical press to press an item)
	 * @param entityScan ticks to wait until to scan for new entities in processing bounding box
	 * @param smartBlockEntity entity to process items
	 */
	public ICanProcessItems(int cycle, int entityScan, SmartBlockEntity smartBlockEntity) {
		super(smartBlockEntity);
		this.cycle = cycle;
		this.entityScan = entityScan;
	}

	public void startProcessing() {

	}

}
