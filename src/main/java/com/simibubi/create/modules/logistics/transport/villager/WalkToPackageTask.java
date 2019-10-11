package com.simibubi.create.modules.logistics.transport.villager;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableMap;
import com.simibubi.create.modules.logistics.transport.CardboardBoxEntity;

import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPosWrapper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;

public class WalkToPackageTask extends Task<VillagerEntity> {
	private List<CardboardBoxEntity> foundPackages = new ArrayList<>();
	private CardboardBoxEntity targetedPackage;

	public WalkToPackageTask() {
		super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.VALUE_ABSENT,
				MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT), 20);
	}

	protected boolean shouldExecute(ServerWorld worldIn, VillagerEntity owner) {
		if (!worldIn.getEntitiesWithinAABB(CardboardBoxEntity.class, owner.getBoundingBox().grow(1.0D, 0.5D, 1.0D),
				e -> !e.isPassenger() && !e.getPersistentData().getBoolean("Delivered")).isEmpty())
			return false;
		this.foundPackages = worldIn.getEntitiesWithinAABB(CardboardBoxEntity.class,
				owner.getBoundingBox().grow(50.0D, 10.0D, 50.0D),
				e -> !e.isPassenger() && !e.getPersistentData().getBoolean("Delivered"));
		return !this.foundPackages.isEmpty();
	}

	protected void startExecuting(ServerWorld worldIn, VillagerEntity entityIn, long gameTimeIn) {
		LogisticianHandler.ponder(entityIn, "Let's go to that package over there");
		targetedPackage = this.foundPackages.get(worldIn.rand.nextInt(this.foundPackages.size()));
		Vec3d vec3d = targetedPackage.getPositionVec();
		entityIn.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosWrapper(new BlockPos(vec3d)));
		entityIn.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vec3d, 0.5F, 0));
	}

	@Override
	protected boolean shouldContinueExecuting(ServerWorld worldIn, VillagerEntity entityIn, long gameTimeIn) {
		return !entityIn.getPosition().withinDistance(targetedPackage.getPosition(), 2);
	}

}
