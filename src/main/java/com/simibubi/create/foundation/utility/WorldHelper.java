package com.simibubi.create.foundation.utility;

import com.simibubi.create.Create;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IBiomeReader;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class WorldHelper {
	public static ResourceLocation getDimensionID(IBiomeReader world) {
		return world.registryAccess()
			.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY)
			.getKey(world.dimensionType());
	}

	@Nullable
	public static <T extends TileEntity> T getTileAt(@Nullable IBlockReader world, @Nullable BlockPos pos) {
		if (world == null || pos == null)
			return null;
		try {
			return (T) world.getBlockEntity(pos);
		} catch (ClassCastException e) {
			return null;
		} catch (Exception e) {
			Create.LOGGER.warn("Invalid getBlockEntity() call: {}", e.getMessage());
			return null;
		}
	}
}
