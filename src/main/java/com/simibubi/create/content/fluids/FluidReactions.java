package com.simibubi.create.content.fluids;

import com.simibubi.create.AllFluids;
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
import net.minecraftforge.fluids.FluidStack;

public class FluidReactions {

	public static void handlePipeFlowCollision(Level world, BlockPos pos, FluidStack fluid, FluidStack fluid2) {
		Fluid f1 = fluid.getFluid();
		Fluid f2 = fluid2.getFluid();
		boolean isBelowDeepslate = pos.getY() < 4;

		AdvancementBehaviour.tryAward(world, pos, AllAdvancements.CROSS_STREAMS);
		BlockHelper.destroyBlock(world, pos, 1);

		if (f1 == Fluids.WATER && f2 == Fluids.LAVA || f2 == Fluids.WATER && f1 == Fluids.LAVA)
			world.setBlockAndUpdate(pos, isBelowDeepslate ? Blocks.COBBLE_DEEPSLATE.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState());
		else if (f1 == Fluids.LAVA && FluidHelper.hasBlockState(f2)) {
			BlockState lavaInteraction = AllFluids.getLavaInteraction(FluidHelper.convertToFlowing(f2)
				.defaultFluidState());
			if (lavaInteraction != null)
				world.setBlockAndUpdate(pos, lavaInteraction);
		} else if (f2 == Fluids.LAVA && FluidHelper.hasBlockState(f1)) {
			BlockState lavaInteraction = AllFluids.getLavaInteraction(FluidHelper.convertToFlowing(f1)
				.defaultFluidState());
			if (lavaInteraction != null)
				world.setBlockAndUpdate(pos, lavaInteraction);
		}
	}

	public static void handlePipeSpillCollision(Level world, BlockPos pos, Fluid pipeFluid, FluidState worldFluid) {
		Fluid pf = FluidHelper.convertToStill(pipeFluid);
		Fluid wf = worldFluid.getType();
		boolean isBelowDeepslate = pos.getY() < 4;
		if (FluidHelper.isTag(pf, FluidTags.WATER) && wf == Fluids.LAVA)
			world.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
		else if (pf == Fluids.WATER && wf == Fluids.FLOWING_LAVA)
			world.setBlockAndUpdate(pos, isBelowDeepslate ? Blocks.COBBLE_DEEPSLATE.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState());
		else if (pf == Fluids.LAVA && wf == Fluids.WATER)
			world.setBlockAndUpdate(pos, isBelowDeepslate ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.STONE.defaultBlockState());
		else if (pf == Fluids.LAVA && wf == Fluids.FLOWING_WATER)
			world.setBlockAndUpdate(pos, isBelowDeepslate ? Blocks.COBBLE_DEEPSLATE.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState());

		if (pf == Fluids.LAVA) {
			BlockState lavaInteraction = AllFluids.getLavaInteraction(worldFluid);
			if (lavaInteraction != null)
				world.setBlockAndUpdate(pos, lavaInteraction);
		} else if (wf == Fluids.FLOWING_LAVA && FluidHelper.hasBlockState(pf)) {
			BlockState lavaInteraction = AllFluids.getLavaInteraction(FluidHelper.convertToFlowing(pf)
				.defaultFluidState());
			if (lavaInteraction != null)
				world.setBlockAndUpdate(pos, lavaInteraction);
		}
	}

}
