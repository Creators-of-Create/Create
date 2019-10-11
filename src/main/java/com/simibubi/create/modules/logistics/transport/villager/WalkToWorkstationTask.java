package com.simibubi.create.modules.logistics.transport.villager;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.modules.logistics.transport.CardboardBoxEntity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;

public class WalkToWorkstationTask extends PackageDeliveryTask {

	public WalkToWorkstationTask() {
		super(60);
	}

	protected boolean shouldExecute(ServerWorld worldIn, VillagerEntity owner) {
		if (!super.shouldExecute(worldIn, owner))
			return false;
		CardboardBoxEntity box = getBox(owner);
		GlobalPos rememberedAddress = LogisticianHandler.getRememberedAddress(owner, box.getAddress());
		return rememberedAddress == null;
	}

	protected void startExecuting(ServerWorld worldIn, VillagerEntity entityIn, long gameTimeIn) {
		LogisticianHandler.ponder(entityIn, "I forgot where this address is at");
		GlobalPos workstation = LogisticianHandler.getJobSite(entityIn);
		if (workstation != null) {
			BlockPos pos = workstation.getPos();
			if (worldIn.isBlockPresent(pos)) {
				BlockState blockState = worldIn.getBlockState(pos);
				if (AllBlocks.LOGISTICIANS_TABLE.typeOf(blockState))
					pos = pos.offset(blockState.get(BlockStateProperties.HORIZONTAL_FACING));
			}
			Vec3d vec = new Vec3d(pos);
			entityIn.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vec, 0.5F, 0));
		}
	}

}
