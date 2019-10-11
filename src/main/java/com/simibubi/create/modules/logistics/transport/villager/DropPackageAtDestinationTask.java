package com.simibubi.create.modules.logistics.transport.villager;

import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.logistics.transport.CardboardBoxEntity;

import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;

public class DropPackageAtDestinationTask extends PackageDeliveryTask {

	private GlobalPos rememberedAddress;

	public DropPackageAtDestinationTask() {
		super(10);
	}

	@Override
	protected boolean shouldExecute(ServerWorld worldIn, VillagerEntity owner) {
		if (!super.shouldExecute(worldIn, owner))
			return false;
		rememberedAddress = LogisticianHandler.getRememberedAddress(owner, getBox(owner).getAddress());
		return rememberedAddress != null && rememberedAddress.getPos().withinDistance(owner.getPosition(), 2);
	}

	@Override
	protected void startExecuting(ServerWorld worldIn, VillagerEntity entityIn, long gameTimeIn) {
		LogisticianHandler.ponder(entityIn, "You're welcome.");
		CardboardBoxEntity box = getBox(entityIn);
		box.stopRiding();
		Vec3d pos = VecHelper.getCenterOf(rememberedAddress.getPos());
		box.setPosition(pos.x, pos.y, pos.z);
		box.getPersistentData().putBoolean("Delivered", true);
	}

}
