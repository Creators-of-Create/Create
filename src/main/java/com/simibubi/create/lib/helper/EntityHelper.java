package com.simibubi.create.lib.helper;

import com.simibubi.create.lib.extensions.EntityExtensions;
import com.simibubi.create.lib.mixin.accessor.EntityAccessor;
import com.simibubi.create.lib.utility.MixinHelper;

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

	public static boolean canBeRidden(Entity entity) {
		return get(entity).create$canBeRidden(entity);
	}

	private static EntityAccessor get(Entity entity) {
		return MixinHelper.cast(entity);
	}

	private EntityHelper() {}
}
