package com.simibubi.create.modules.logistics.transport.villager;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableMap;
import com.simibubi.create.modules.logistics.transport.CardboardBoxEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.world.server.ServerWorld;

public class CollectPackageTask extends Task<VillagerEntity> {
	private List<CardboardBoxEntity> foundPackages = new ArrayList<>();

	public CollectPackageTask() {
		super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.VALUE_ABSENT,
				MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT), 1);
	}

	protected boolean shouldExecute(ServerWorld worldIn, VillagerEntity owner) {
		this.foundPackages = worldIn.getEntitiesWithinAABB(CardboardBoxEntity.class,
				owner.getBoundingBox().grow(1.0D, 0.5D, 1.0D),
				e -> !e.isPassenger() && !e.getPersistentData().getBoolean("Delivered"));
		return !this.foundPackages.isEmpty();
	}

	protected void startExecuting(ServerWorld worldIn, VillagerEntity entityIn, long gameTimeIn) {
		LogisticianHandler.ponder(entityIn, "Yoink!");
		CardboardBoxEntity box = this.foundPackages.get(worldIn.rand.nextInt(this.foundPackages.size()));
		Entity e = entityIn;
		while (!e.getPassengers().isEmpty())
			e = e.getPassengers().get(0);
		box.startRiding(e);
	}

}
