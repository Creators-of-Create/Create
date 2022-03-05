package com.simibubi.create.content.curiosities.armor;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.utility.NBTHelper;
import io.github.fabricators_of_create.porting_lib.mixin.common.accessor.LivingEntityAccessor;
import io.github.fabricators_of_create.porting_lib.util.EntityHelper;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class DivingBootsItem extends CopperArmorItem {

	public DivingBootsItem(Properties p_i48534_3_) {
		super(EquipmentSlot.FEET, p_i48534_3_);
	}

	public static void accellerateDescentUnderwater(LivingEntity entity) {
//		LivingEntity entity = event.getEntityLiving();
		if (!affects(entity))
			return;

		Vec3 motion = entity.getDeltaMovement();
//		Boolean isJumping = ObfuscationReflectionHelper.getPrivateValue(LivingEntity.class, entity, "f_20899_"); // jumping
		boolean isJumping = ((LivingEntityAccessor) entity).port_lib$isJumping();
		entity.setOnGround(entity.isOnGround() || entity.verticalCollision);

		if (isJumping && entity.isOnGround()) {
			motion = motion.add(0, .5f, 0);
			entity.setOnGround(false);
		} else {
			motion = motion.add(0, -0.05f, 0);
		}

		float multiplier = 1.3f;
		if (motion.multiply(1, 0, 1)
			.length() < 0.145f && (entity.zza > 0 || entity.xxa != 0) && !entity.isShiftKeyDown())
			motion = motion.multiply(multiplier, 1, multiplier);
		entity.setDeltaMovement(motion);
	}

	protected static boolean affects(LivingEntity entity) {
		if (!AllItems.DIVING_BOOTS.get()
			.isWornBy(entity)) {
			EntityHelper.getExtraCustomData(entity)
				.remove("HeavyBoots");
			return false;
		}

		NBTHelper.putMarker(EntityHelper.getExtraCustomData(entity), "HeavyBoots");
		if (!entity.isInWater())
			return false;
		if (entity.getPose() == Pose.SWIMMING)
			return false;
		if (entity instanceof Player) {
			Player playerEntity = (Player) entity;
			if (playerEntity.getAbilities().flying)
				return false;
		}
		return true;
	}

}
