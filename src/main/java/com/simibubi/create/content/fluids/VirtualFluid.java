package com.simibubi.create.content.fluids;

import com.simibubi.create.content.fluids.potion.PotionFluid;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public class VirtualFluid extends ForgeFlowingFluid {

	public static VirtualFluid createSource(Properties properties) {
		return new VirtualFluid(properties, true);
	}

	public static VirtualFluid createFlowing(Properties properties) {
		return new VirtualFluid(properties, false);
	}


	private final boolean source;

	public VirtualFluid(Properties properties, boolean source) {
		super(properties);
		this.source = source;
	}

	@Override
	public Fluid getSource() {
		if (source) {
			return this;
		}
		return super.getSource();
	}

	@Override
	public Fluid getFlowing() {
		if (source) {
			return super.getFlowing();
		}
		return this;
	}

	@Override
	public Item getBucket() {
		return Items.AIR;
	}

	@Override
	protected BlockState createLegacyBlock(FluidState state) {
		return Blocks.AIR.defaultBlockState();
	}

	@Override
	public boolean isSource(FluidState p_207193_1_) {
		return source;
	}

	@Override
	public int getAmount(FluidState p_207192_1_) {
		return 0;
	}

}
