package com.simibubi.create.foundation.tileEntity;

import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;

public class ComparatorUtil {

	public static int fractionToRedstoneLevel(double frac) {
		return MathHelper.floor(MathHelper.clamp(frac * 14 + (frac > 0 ? 1 : 0), 0, 15));
	}

	public static int levelOfSmartFluidTank(IBlockReader world, BlockPos pos) {
		SmartFluidTankBehaviour fluidBehaviour = TileEntityBehaviour.get(world, pos, SmartFluidTankBehaviour.TYPE);
		if (fluidBehaviour == null)
			return 0;
		SmartFluidTank primaryHandler = fluidBehaviour.getPrimaryHandler();
		double fillFraction = (double) primaryHandler.getFluid()
			.getAmount() / primaryHandler.getCapacity();
		return fractionToRedstoneLevel(fillFraction);
	}

}
