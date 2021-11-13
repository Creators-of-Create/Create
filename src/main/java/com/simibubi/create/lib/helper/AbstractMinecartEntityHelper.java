package com.simibubi.create.lib.helper;

import com.simibubi.create.lib.extensions.AbstractMinecartExtensions;
import com.simibubi.create.lib.mixin.accessor.AbstractMinecartAccessor;
import com.simibubi.create.lib.utility.MixinHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;

public final class AbstractMinecartEntityHelper {
	public static void moveMinecartOnRail(AbstractMinecart entity, BlockPos pos) {
		((AbstractMinecartExtensions) MixinHelper.cast(entity)).create$moveMinecartOnRail(pos);
	}

	public static ItemStack getCartItem(AbstractMinecart entity) {
		return ((AbstractMinecartExtensions) MixinHelper.cast(entity)).create$getCartItem();
	}

	public static double getMaximumSpeed(AbstractMinecart entity) {
		return ((AbstractMinecartAccessor) MixinHelper.cast(entity)).create$getMaxSpeed();
	}

	public static float getMaximumSpeedF(AbstractMinecart entity) {
		return (float) getMaximumSpeed(entity);
	}

	public static boolean canCartUseRail(AbstractMinecart entity) {
		return ((AbstractMinecartExtensions) entity).create$canUseRail();
	}

	public static BlockPos getCurrentRailPos(AbstractMinecart cart) {
		return ((AbstractMinecartExtensions) cart).create$getCurrentRailPos();
	}

	private AbstractMinecartEntityHelper() {}
}
