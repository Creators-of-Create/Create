package com.simibubi.create.modules.logistics;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.Create;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.world.IWorld;

public class InWorldItemProcessingHandler {

	private Map<IWorld, Map<ItemEntity, InWorldProcessing>> items = new HashMap<>();

	public void onLoadWorld(IWorld world) {
		items.put(world, new HashMap<>());
		Create.logger.info("Prepared Item Processing space for " + world.getDimension().getType().getRegistryName());
	}

	public void onUnloadWorld(IWorld world) {
		items.remove(world);
		Create.logger.info("Removed Item Processing space for " + world.getDimension().getType().getRegistryName());
	}

	public void startProcessing(ItemEntity entity, InWorldProcessing processing) {
		Map<ItemEntity, InWorldProcessing> itemsInWorld = items.get(entity.world);
		if (itemsInWorld.containsKey(entity) && processing.type == itemsInWorld.get(entity).type) {
			itemsInWorld.get(entity).processorCount++;
		} else {
			itemsInWorld.put(entity, processing);
		}
	}

	public void stopProcessing(ItemEntity entity) {
		Map<ItemEntity, InWorldProcessing> itemsInWorld = items.get(entity.world);
		if (!itemsInWorld.containsKey(entity))
			return;
		InWorldProcessing processing = itemsInWorld.get(entity);
		processing.processorCount--;

		if (processing.processorCount == 0)
			itemsInWorld.remove(entity);
	}

	public InWorldProcessing getProcessing(ItemEntity entity) {
		Map<ItemEntity, InWorldProcessing> itemsInWorld = items.get(entity.world);
		if (!itemsInWorld.containsKey(entity))
			return null;
		return itemsInWorld.get(entity);
	}

}
