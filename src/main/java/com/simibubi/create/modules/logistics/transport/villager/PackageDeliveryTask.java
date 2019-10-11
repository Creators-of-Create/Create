package com.simibubi.create.modules.logistics.transport.villager;

import java.util.List;

import com.google.common.collect.ImmutableMap;
import com.simibubi.create.modules.logistics.transport.CardboardBoxEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.world.server.ServerWorld;

public class PackageDeliveryTask extends Task<VillagerEntity> {

	public PackageDeliveryTask(int timeout) {
		super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.VALUE_ABSENT,
				MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT), timeout);
	}

	protected boolean shouldExecute(ServerWorld worldIn, VillagerEntity owner) {
		return getBox(owner) != null;
	}
	
	protected CardboardBoxEntity getBox(VillagerEntity owner) {
		List<Entity> passengers = owner.getPassengers();
		if (passengers.isEmpty())
			return null;
		Entity entity = passengers.get(0);
		if (!(entity instanceof CardboardBoxEntity))
			return null;
		return (CardboardBoxEntity) entity;
	}

}
