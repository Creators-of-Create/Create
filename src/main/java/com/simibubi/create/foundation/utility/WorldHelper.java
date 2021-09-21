package com.simibubi.create.foundation.utility;

import com.simibubi.create.Create;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

public class WorldHelper {
	public static ResourceLocation getDimensionID(LevelAccessor world) {
		return world.registryAccess()
			.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY)
			.getKey(world.dimensionType());
	}

	@Nullable
	public static <T extends BlockEntity> T getTileAt(@Nullable LevelAccessor world, @Nullable BlockPos pos) {
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

	@Nullable
	public static <T extends BlockEntity> T getTileAt(@Nullable BlockGetter world, @Nullable BlockPos pos) {
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
