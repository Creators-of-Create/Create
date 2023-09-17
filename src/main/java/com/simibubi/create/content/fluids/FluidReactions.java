package com.simibubi.create.content.fluids;

import com.simibubi.create.api.event.PipeCollisionEvent;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.BlockHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;

public class FluidReactions {

	public static void handlePipeFlowCollision(Level level, BlockPos pos, FluidStack fluid, FluidStack fluid2) {
		Fluid f1 = fluid.getFluid();
		Fluid f2 = fluid2.getFluid();

		AdvancementBehaviour.tryAward(level, pos, AllAdvancements.CROSS_STREAMS);
		BlockHelper.destroyBlock(level, pos, 1);

		PipeCollisionEvent.Flow event = new PipeCollisionEvent.Flow(level, pos, f1, f2, null);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.getState() != null) {
			level.setBlockAndUpdate(pos, event.getState());
		}
	}

	public static void handlePipeSpillCollision(Level level, BlockPos pos, Fluid pipeFluid, FluidState worldFluid) {
		Fluid pf = FluidHelper.convertToStill(pipeFluid);
		Fluid wf = worldFluid.getType();

		PipeCollisionEvent.Spill event = new PipeCollisionEvent.Spill(level, pos, wf, pf, null);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.getState() != null) {
			level.setBlockAndUpdate(pos, event.getState());
		}
	}

}
