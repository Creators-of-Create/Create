package com.simibubi.create.content.contraptions.fluids;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public class VirtualFluid extends ForgeFlowingFluid {

	public VirtualFluid(Properties properties) {
		super(properties);
	}

	@Override
	public Fluid getStillFluid() {
		return super.getStillFluid();
	}

	@Override
	public Fluid getFlowingFluid() {
		return this;
	}

	@Override
	public Item getFilledBucket() {
		return Items.AIR;
	}

	@Override
	protected BlockState getBlockState(FluidState state) {
		return Blocks.AIR.getDefaultState();
	}

	@Override
	public boolean isSource(FluidState p_207193_1_) {
		return false;
	}

	@Override
	public int getLevel(FluidState p_207192_1_) {
		return 0;
	}

}
