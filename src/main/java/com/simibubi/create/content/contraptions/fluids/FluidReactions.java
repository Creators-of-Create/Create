package com.simibubi.create.content.contraptions.fluids;

import com.simibubi.create.AllFluids;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.BlockHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public class FluidReactions {

	public static void handlePipeFlowCollision(World world, BlockPos pos, FluidStack fluid, FluidStack fluid2) {
		Fluid f1 = fluid.getFluid();
		Fluid f2 = fluid2.getFluid();
		BlockHelper.destroyBlock(world, pos, 1);
		if (f1 == Fluids.WATER && f2 == Fluids.LAVA || f2 == Fluids.WATER && f1 == Fluids.LAVA)
			world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());
		else if (f1 == Fluids.LAVA && FluidHelper.hasBlockState(f2)) {
			BlockState lavaInteraction = AllFluids.getLavaInteraction(FluidHelper.convertToFlowing(f2)
				.getDefaultState());
			if (lavaInteraction != null)
				world.setBlockState(pos, lavaInteraction);
		} else if (f2 == Fluids.LAVA && FluidHelper.hasBlockState(f1)) {
			BlockState lavaInteraction = AllFluids.getLavaInteraction(FluidHelper.convertToFlowing(f1)
				.getDefaultState());
			if (lavaInteraction != null)
				world.setBlockState(pos, lavaInteraction);
		}
	}

	public static void handlePipeSpillCollision(World world, BlockPos pos, Fluid pipeFluid, FluidState worldFluid) {
		Fluid pf = FluidHelper.convertToStill(pipeFluid);
		Fluid wf = worldFluid.getFluid();
		if (pf.isIn(FluidTags.WATER) && wf == Fluids.LAVA)
			world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState());
		else if (pf == Fluids.WATER && wf == Fluids.FLOWING_LAVA)
			world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());
		else if (pf == Fluids.LAVA && wf == Fluids.WATER)
			world.setBlockState(pos, Blocks.STONE.getDefaultState());
		else if (pf == Fluids.LAVA && wf == Fluids.FLOWING_WATER)
			world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());

		if (pf == Fluids.LAVA) {
			BlockState lavaInteraction = AllFluids.getLavaInteraction(worldFluid);
			if (lavaInteraction != null)
				world.setBlockState(pos, lavaInteraction);
		} else if (wf == Fluids.FLOWING_LAVA && FluidHelper.hasBlockState(pf)) {
			BlockState lavaInteraction = AllFluids.getLavaInteraction(FluidHelper.convertToFlowing(pf)
				.getDefaultState());
			if (lavaInteraction != null)
				world.setBlockState(pos, lavaInteraction);
		}
	}

}
