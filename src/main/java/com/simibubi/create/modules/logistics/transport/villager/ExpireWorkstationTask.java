package com.simibubi.create.modules.logistics.transport.villager;

import com.google.common.collect.ImmutableMap;
import com.simibubi.create.AllBlocks;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.server.ServerWorld;

public class ExpireWorkstationTask extends Task<VillagerEntity> {

	private GlobalPos workstationPos;

	public ExpireWorkstationTask() {
		super(ImmutableMap.of(), 20);
	}

	@Override
	protected boolean shouldExecute(ServerWorld worldIn, VillagerEntity owner) {
		workstationPos = LogisticianHandler.getJobSite(owner);
		return workstationPos.getPos().withinDistance(owner.getPosition(), 5);
	}

	@Override
	protected void startExecuting(ServerWorld worldIn, VillagerEntity entityIn, long gameTimeIn) {
		ServerWorld serverworld = worldIn.getServer().getWorld(workstationPos.getDimension());
		if (!AllBlocks.LOGISTICIANS_TABLE.typeOf(serverworld.getBlockState(workstationPos.getPos()))) {
			LogisticianHandler.ponder(entityIn, "Oh no! My workstation!");
			entityIn.setVillagerData(entityIn.getVillagerData().withProfession(VillagerProfession.NONE).withLevel(1));
			entityIn.resetBrain(serverworld);
			entityIn.getPassengers().forEach(Entity::stopRiding);
		}
	}

}
