package com.simibubi.create.content.itemprocessing;


import com.simibubi.create.Create;
import com.simibubi.create.content.itemprocessing.specifics.ICanProcessInWorldItems;
import com.simibubi.create.content.itemprocessing.specifics.ProcessingSpecifics;
import com.simibubi.create.content.kinetics.IHaveKineticSpeed;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.processing.ProcessingMode;
import com.simibubi.create.content.processing.basin.BasinBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Abstraction of machines which can process items
 *
 * Works by using a ticking mechanic.
 * Each tick the finishedTicks is counted up, depending on kinetic speed if provided.
 *
 * When the finishedTicks cross the threshold (cycle) the process is finished, and the next one is started.
 *
 * This class only handles the processing of items. This means how many ticks it will take etc.
 * It will also, if the specifics have it, try to start in world processing.
 *
 * Belt processing is handled in the super class
 * @see BeltProcessingBehaviour
 * @param <T> the processing specifics.
 * @see ProcessingSpecifics
 */
public abstract class ICanProcessItems <T extends ProcessingSpecifics> extends BeltProcessingBehaviour {

	private final int cycle;
	private final int entityScan;

	private int entityScanCooldown;

	private final T specifics;

	private boolean isProcessing = false;

	/**
	 * used to track ticks finished on client or something
	 */
	private int prevFinishedTicks = 0;
	/**
	 * ticks this process has already finished
	 */
	private int finishedTicks = 0;

	private ProcessingMode mode;

	/**
	 * @param cycle ticks it takes to process (for example mechanical press to press an item)
	 * @param entityScan ticks to wait until to scan for new entities in processing bounding box
	 * @param smartBlockEntity entity to process items
	 */
	public ICanProcessItems(int cycle, int entityScan, SmartBlockEntity smartBlockEntity, T specifics) {
		super(smartBlockEntity);
		this.cycle = cycle;
		this.entityScan = entityScan;
		this.entityScanCooldown = entityScan;
		this.specifics = specifics;
	}


	@Override
	public void tick() {
		super.tick();

		Level level = getWorld();
		BlockPos worldPosition = getPos();

		if (level == null) {
			return;
		}

		boolean canProcessItemsInWorld = specifics instanceof ICanProcessInWorldItems;

		//item in world processing
		if (!level.isClientSide && !isProcessing && canProcessItemsInWorld) {
			if (specifics instanceof IHaveKineticSpeed && ((IHaveKineticSpeed) specifics).getKineticSpeed() == 0) {
				return;
			}

			if (entityScanCooldown > 0) {
				entityScanCooldown--;
				return;
			}

			entityScanCooldown = entityScan;

			boolean isAboveBelt = BlockEntityBehaviour.get(level, worldPosition.below(2), TransportedItemStackHandlerBehaviour.TYPE) != null;
			if (isAboveBelt) {
				return;
			}

			if (BasinBlock.isBasin(level, worldPosition.below(2))) {
				return;
			}


			AABB boundingBox = new AABB(worldPosition.below()).deflate(.125f);

			for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, boundingBox)) {
				if (!itemEntity.isAlive() || !itemEntity.onGround())
					continue;

				if (!((ICanProcessInWorldItems) specifics).tryProcessItemInWorld(itemEntity, true))
					continue;

				startProcessing(ProcessingMode.WORLD);
				return;
			}
		}

		if (!isProcessing) {
			return;
		}

		//animation sync with client????
		if (level.isClientSide && (finishedTicks == -cycle / 2)) {
			prevFinishedTicks = cycle / 2;
			return;
		}

		onProcessTick(level, finishedTicks, cycle);

		boolean isProcessedFinished = finishedTicks > cycle;
		if (!level.isClientSide && isProcessedFinished) {
			setProcessTicks(0);
			setProcessing(false);
			specifics.onFinished();
			onProcessedFinish();
			return;
		}

		prevFinishedTicks = finishedTicks;
		finishedTicks += getScaledProcessingTicks();
		modifyProcessingTicks(level, prevFinishedTicks, finishedTicks, cycle);
	}

	/**
	 * each valid process tick for this process
	 * @see ICanProcessItems
	 * @param level where the process is happening
	 * @param finishedTicks ticks already finished for this process
	 * @param cycle ticks required for this process
	 */
	public void onProcessTick(Level level, int finishedTicks, int cycle) {};

	/**
	 * Called when the process has finished processing
	 */
	public void onProcessedFinish() {}

	/**
	 * Called when the processing has started
	 * @param mode the mode
	 */
	public void onProcessStarted(ProcessingMode mode) {};

	/**
	 * Calculates how many process ticks should be done per tick.
	 * This can for example scale with the kinetic speed of the block
	 * @return processing ticks per tick
	 */
	public abstract int getScaledProcessingTicks();

	/**
	 * Allows for custom tick modification if the process needs to wait for receiving packets
	 * @param level the process is happening
	 * @param prevTicks ticks finished and received from client
	 * @param ticks ticks finished on server side
	 * @param cycle ticks it takes to finish process
	 */
	public void modifyProcessingTicks(Level level, int prevTicks, int ticks, int cycle) {
	}

	public void setFinishedTicks(int finishedTicks) {
		this.finishedTicks = finishedTicks;
	}

	/**
	 * Sets both the finished ticks and the prev finished ticks
	 * @param ticks new value
	 */
	public void setProcessTicks(int ticks) {
		this.finishedTicks = ticks;
		this.prevFinishedTicks = ticks;
	}

	public int getFinishedTicks() {
		return finishedTicks;
	}

	public int getPrevFinishedTicks() {
		return prevFinishedTicks;
	}

	public void startProcessing(ProcessingMode mode) {
		setProcessTicks(0);
		setProcessing(true);
		onProcessStarted(mode);
	}

	public T getSpecifics() {
		return specifics;
	}

	/**
	 * @return whether the machine is currently processing an item
	 */
	public boolean isProcessing() {
		return isProcessing;
	}

	/**
	 * WARN, use this method carefully to overwrite processing state
	 * @param processing new value
	 */
	public void setProcessing(boolean processing) {
		isProcessing = processing;
		Create.LOGGER.info(processing ? "Processing started" : "Processing stopped");
	}
}
