package com.simibubi.create.content.curiosities.armor;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

@EventBusSubscriber
public class DivingBootsItem extends CopperArmorItem {

	public DivingBootsItem(Properties p_i48534_3_) {
		super(EquipmentSlotType.FEET, p_i48534_3_);
	}

	@SubscribeEvent
	public static void accellerateDescentUnderwater(LivingUpdateEvent event) {
		LivingEntity entity = event.getEntityLiving();
		if (!affects(entity))
			return;

		Vector3d motion = entity.getMotion();
		Boolean isJumping = ObfuscationReflectionHelper.getPrivateValue(LivingEntity.class, entity, "field_70703_bu");
		entity.onGround |= entity.collidedVertically;

		if (isJumping && entity.onGround) {
			motion = motion.add(0, .5f, 0);
			entity.onGround = false;
		} else {
			motion = motion.add(0, -0.05f, 0);
		}

		float multiplier = 1.3f;
		if (motion.mul(1, 0, 1)
			.length() < 0.145f && (entity.moveForward > 0 || entity.moveStrafing != 0) && !entity.isSneaking())
			motion = motion.mul(multiplier, 1, multiplier);
		entity.setMotion(motion);
	}

	protected static boolean affects(LivingEntity entity) {
		if (!AllItems.DIVING_BOOTS.get()
			.isWornBy(entity)) {
			entity.getPersistentData()
				.remove("HeavyBoots");
			return false;
		}

		NBTHelper.putMarker(entity.getPersistentData(), "HeavyBoots");
		if (!entity.isInWater())
			return false;
		if (entity.getPose() == Pose.SWIMMING)
			return false;
		if (entity instanceof PlayerEntity) {
			PlayerEntity playerEntity = (PlayerEntity) entity;
			if (playerEntity.abilities.isFlying)
				return false;
		}
		return true;
	}

}
