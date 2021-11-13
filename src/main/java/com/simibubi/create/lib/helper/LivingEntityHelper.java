package com.simibubi.create.lib.helper;

import com.simibubi.create.lib.mixin.accessor.LivingEntityAccessor;
import com.simibubi.create.lib.utility.MixinHelper;

import net.minecraft.world.entity.LivingEntity;

public class LivingEntityHelper {
	public static boolean isFlying(LivingEntity entity) {
		return get(entity).create$isJumping();
	}

	private static LivingEntityAccessor get(LivingEntity entity) {
		return MixinHelper.cast(entity);
	}

	private LivingEntityHelper() {}
}
