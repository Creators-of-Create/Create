package com.simibubi.create.lib.util;

import com.simibubi.create.lib.extensions.BlockEntityExtensions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TileEntityHelper {
	public static final String EXTRA_DATA_KEY = "ForgeData";

	public static CompoundTag getExtraCustomData(BlockEntity tileEntity) {
		return ((BlockEntityExtensions) tileEntity).create$getExtraCustomData();
	}
}
