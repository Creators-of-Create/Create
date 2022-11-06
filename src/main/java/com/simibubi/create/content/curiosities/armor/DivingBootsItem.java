package com.simibubi.create.content.curiosities.armor;

import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class DivingBootsItem extends BaseArmorItem {
	public static final EquipmentSlot SLOT = EquipmentSlot.FEET;

	public DivingBootsItem(ArmorMaterial material, Properties properties, ResourceLocation textureLoc) {
		super(material, SLOT, properties, textureLoc);
	}

	public static boolean isWornBy(Entity entity) {
		if (!(entity instanceof LivingEntity livingEntity)) {
			return false;
		}
		return livingEntity.getItemBySlot(SLOT).getItem() instanceof DivingBootsItem;
	}

	@SubscribeEvent
	public static void accellerateDescentUnderwater(LivingUpdateEvent event) {
		LivingEntity entity = event.getEntityLiving();
		if (!affects(entity))
			return;

		Vec3 motion = entity.getDeltaMovement();
		boolean isJumping = entity.jumping;
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
		if (!isWornBy(entity)) {
			entity.getPersistentData()
				.remove("HeavyBoots");
			return false;
		}

		NBTHelper.putMarker(entity.getPersistentData(), "HeavyBoots");
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
