package com.simibubi.create.content.fluids;

import com.simibubi.create.AllFluids;
import com.simibubi.create.api.event.PipeCollisionEvent;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.BlockHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class FluidReactions {

	public static void handlePipeFlowCollision(Level level, BlockPos pos, FluidStack fluid, FluidStack fluid2) {
		Fluid f1 = fluid.getFluid();
		Fluid f2 = fluid2.getFluid();

		AdvancementBehaviour.tryAward(level, pos, AllAdvancements.CROSS_STREAMS);
		BlockHelper.destroyBlock(level, pos, 1);

		PipeCollisionEvent.Flow event = new PipeCollisionEvent.Flow(level, pos, f1, f2, null);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.getState() != null)
			level.setBlockAndUpdate(pos, event.getState());
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void handlePipeFlowCollisionFallback(PipeCollisionEvent.Flow event) {
		Fluid f1 = event.getFirstFluid();
		Fluid f2 = event.getSecondFluid();

		if (f1 == Fluids.WATER && f2 == Fluids.LAVA || f2 == Fluids.WATER && f1 == Fluids.LAVA) {
			event.setState(Blocks.COBBLESTONE.defaultBlockState());
		} else if (f1 == Fluids.LAVA && FluidHelper.hasBlockState(f2)) {
			BlockState lavaInteraction = AllFluids.getLavaInteraction(FluidHelper.convertToFlowing(f2).defaultFluidState());
			if (lavaInteraction != null) {
				event.setState(lavaInteraction);
			}
		} else if (f2 == Fluids.LAVA && FluidHelper.hasBlockState(f1)) {
			BlockState lavaInteraction = AllFluids.getLavaInteraction(FluidHelper.convertToFlowing(f1).defaultFluidState());
			if (lavaInteraction != null) {
				event.setState(lavaInteraction);
			}
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

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void handlePipeSpillCollisionFallback(PipeCollisionEvent.Spill event) {
		Fluid pf = event.getPipeFluid();
		Fluid wf = event.getWorldFluid();

		if (FluidHelper.isTag(pf, FluidTags.WATER) && wf == Fluids.LAVA) {
			event.setState(Blocks.OBSIDIAN.defaultBlockState());
		} else if (pf == Fluids.WATER && wf == Fluids.FLOWING_LAVA) {
			event.setState(Blocks.COBBLESTONE.defaultBlockState());
		} else if (pf == Fluids.LAVA && wf == Fluids.WATER) {
			event.setState(Blocks.STONE.defaultBlockState());
		} else if (pf == Fluids.LAVA && wf == Fluids.FLOWING_LAVA) {
			event.setState(Blocks.COBBLESTONE.defaultBlockState());
		}

		if (pf == Fluids.LAVA) {
			BlockState lavaInteraction = AllFluids.getLavaInteraction(wf.defaultFluidState());
			if (lavaInteraction != null) {
				event.setState(lavaInteraction);
			}
		} else if (wf == Fluids.FLOWING_LAVA && FluidHelper.hasBlockState(pf)) {
			BlockState lavaInteraction = AllFluids.getLavaInteraction(FluidHelper.convertToFlowing(pf).defaultFluidState());
			if (lavaInteraction != null) {
				event.setState(lavaInteraction);
			}
		}
	}

}
