package com.simibubi.create.content.curiosities.tools;

import java.util.Random;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class SandPaperEffectsHandler {
	public static void onUseTick(LivingEntity entity, Random random) {

		spawnItemParticles(entity, random);

		if (shouldPlaySound(entity))
			entity.playSound(entity.getEatingSound(entity.getUseItem()), 0.9F + 0.2F * random.nextFloat(), random.nextFloat() * 0.2F + 0.9F);
	}

	private static boolean shouldPlaySound(LivingEntity entity) {
		// after 6 ticks play the sound every 7th
		return (getTicksUsed(entity) - 6) % 7 == 0;
	}

	private static int getTicksUsed(LivingEntity entity) {
		int useDuration = entity.getUseItem()
				.getUseDuration();
		return useDuration - entity.getUseItemRemainingTicks();
	}

	private static void spawnItemParticles(LivingEntity entity, Random random) {
		ItemStack sanding = entity.getItemInHand(getNonInteractionHand(entity));

		Vec3 vec3 = new Vec3(((double)random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
		vec3 = vec3.xRot(-entity.getXRot() * ((float)Math.PI / 180F));
		vec3 = vec3.yRot(-entity.getYRot() * ((float)Math.PI / 180F));
		double d0 = (double)(-random.nextFloat()) * 0.6D - 0.3D;
		Vec3 vec31 = new Vec3(((double)random.nextFloat() - 0.5D) * 0.3D, d0, 0.6D);
		vec31 = vec31.xRot(-entity.getXRot() * ((float)Math.PI / 180F));
		vec31 = vec31.yRot(-entity.getYRot() * ((float)Math.PI / 180F));
		vec31 = vec31.add(entity.getX(), entity.getEyeY(), entity.getZ());
		if (entity.level instanceof ServerLevel) //Forge: Fix MC-2518 spawnParticle is nooped on server, need to use server specific variant
			((ServerLevel)entity.level).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, sanding), vec31.x, vec31.y, vec31.z, 1, vec3.x, vec3.y + 0.05D, vec3.z, 0.0D);
		else
			entity.level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, sanding), vec31.x, vec31.y, vec31.z, vec3.x, vec3.y + 0.05D, vec3.z);

	}

	private static InteractionHand getNonInteractionHand(LivingEntity entity) {
		return entity.getUsedItemHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
	}
}
