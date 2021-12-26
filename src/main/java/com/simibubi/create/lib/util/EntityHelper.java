package com.simibubi.create.lib.util;

import com.simibubi.create.lib.extensions.EntityExtensions;
import com.simibubi.create.lib.mixin.common.accessor.EntityAccessor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public final class EntityHelper {
	public static final String EXTRA_DATA_KEY = "create_ExtraEntityData";

	public static CompoundTag getExtraCustomData(Entity entity) {
		return ((EntityExtensions) entity).create$getExtraCustomData();
	}

	public static String getEntityString(Entity entity) {
		return ((EntityAccessor) entity).create$getEntityString();
	}

	private EntityHelper() {}
}
