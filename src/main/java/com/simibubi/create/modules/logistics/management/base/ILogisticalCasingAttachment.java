package com.simibubi.create.modules.logistics.management.base;

import javax.annotation.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public interface ILogisticalCasingAttachment {

	public void onCasingUpdated(IWorld world, BlockPos pos, @Nullable LogisticalCasingTileEntity te);

}
