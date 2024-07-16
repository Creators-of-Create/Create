package com.simibubi.create.content.fluids.pipes;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.LEVEL_HONEY;

import com.simibubi.create.AllFluids;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

public class VanillaFluidTargets {

	public static boolean canProvideFluidWithoutCapability(BlockState state) {
		if (state.hasProperty(BlockStateProperties.LEVEL_HONEY))
			return true;
		if (state.is(Blocks.CAULDRON))
			return true;
		if (state.is(Blocks.LAVA_CAULDRON))
			return true;
		if (state.is(Blocks.WATER_CAULDRON))
			return true;
		return false;
	}

	public static FluidStack drainBlock(Level level, BlockPos pos, BlockState state, boolean simulate) {
		if (state.hasProperty(BlockStateProperties.LEVEL_HONEY) && state.getValue(LEVEL_HONEY) >= 5) {
			if (!simulate)
				level.setBlock(pos, state.setValue(LEVEL_HONEY, 0), 3);
			return new FluidStack(AllFluids.HONEY.get()
				.getSource(), 250);
		}
		
		if (state.is(Blocks.LAVA_CAULDRON)) {
			if (!simulate)
				level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
			return new FluidStack(Fluids.LAVA, 1000);
		}
		
		if (state.is(Blocks.WATER_CAULDRON) && state.getBlock() instanceof LayeredCauldronBlock lcb) {
			if (!lcb.isFull(state))
				return FluidStack.EMPTY;
			if (!simulate)
				level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
			return new FluidStack(Fluids.WATER, 1000);
		}
		
		return FluidStack.EMPTY;
	}

}
