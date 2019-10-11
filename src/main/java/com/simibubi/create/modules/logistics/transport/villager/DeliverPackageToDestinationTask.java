package com.simibubi.create.modules.logistics.transport.villager;

import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPosWrapper;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;

public class DeliverPackageToDestinationTask extends PackageDeliveryTask {

	private GlobalPos rememberedAddress;

	public DeliverPackageToDestinationTask() {
		super(60);
	}

	@Override
	protected boolean shouldExecute(ServerWorld worldIn, VillagerEntity owner) {
		if (!super.shouldExecute(worldIn, owner))
			return false;
		rememberedAddress = LogisticianHandler.getRememberedAddress(owner, getBox(owner).getAddress());
		return rememberedAddress != null && !rememberedAddress.getPos().withinDistance(owner.getPosition(), 2);
	}

	@Override
	protected void startExecuting(ServerWorld worldIn, VillagerEntity entityIn, long gameTimeIn) {
		LogisticianHandler.ponder(entityIn, "I know where this is!");
		BlockPos pos = rememberedAddress.getPos();
		Vec3d vec3d = new Vec3d(pos);
		entityIn.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosWrapper(new BlockPos(vec3d)));
		entityIn.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vec3d, 0.5F, 2));
	}

}
