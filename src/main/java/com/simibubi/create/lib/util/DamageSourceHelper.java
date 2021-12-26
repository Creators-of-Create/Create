package com.simibubi.create.lib.util;

import com.simibubi.create.lib.mixin.common.accessor.DamageSourceAccessor;

import net.minecraft.world.damagesource.DamageSource;

public final class DamageSourceHelper {
	public static DamageSource create$createDamageSource(String string) {
		return DamageSourceAccessor.create$init(string);
	}

	// this is probably going to crash and burn.
	public static DamageSource create$createArmorBypassingDamageSource(String string) {
		return MixinHelper.<DamageSourceAccessor>cast(create$createDamageSource(string)).create$setDamageBypassesArmor();
	}

	public static DamageSource create$createFireDamageSource(String string) {
		return MixinHelper.<DamageSourceAccessor>cast(create$createDamageSource(string)).create$setFireDamage();
	}

	private DamageSourceHelper() {}
}
