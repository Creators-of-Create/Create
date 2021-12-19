package com.simibubi.create.lib.helper;

import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import com.simibubi.create.lib.extensions.EntityExtensions;
import com.simibubi.create.lib.mixin.accessor.EntityAccessor;
import com.simibubi.create.lib.utility.MixinHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

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
