package com.simibubi.create.modules.curiosities;

import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ShadowSteelItem extends Item {

	public ShadowSteelItem(Properties properties) {
		super(properties);
	}

	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
		World world = entity.world;
		Vec3d pos = entity.getPositionVec();

		if (world.isRemote && entity.hasNoGravity()) {
			if (world.rand.nextFloat() < MathHelper.clamp(entity.getItem().getCount() - 10,
					Math.min(entity.getMotion().y * 20, 20), 100) / 64f) {
				Vec3d ppos = VecHelper.offsetRandomly(pos, world.rand, .5f);
				world.addParticle(ParticleTypes.END_ROD, ppos.x, pos.y, ppos.z, 0, -.1f, 0);
			}

			if (!entity.getPersistentData().contains("ClientAnimationPlayed")) {
				Vec3d basemotion = new Vec3d(0, 1, 0);
				world.addParticle(ParticleTypes.FLASH, pos.x, pos.y, pos.z, 0, 0, 0);
				for (int i = 0; i < 20; i++) {
					Vec3d motion = VecHelper.offsetRandomly(basemotion, world.rand, 1);
					world.addParticle(ParticleTypes.WITCH, pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
					world.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
				}
				entity.getPersistentData().putBoolean("ClientAnimationPlayed", true);
			}

			return false;
		}

		if (!entity.getPersistentData().contains("FromVoid"))
			return false;

		entity.setNoGravity(true);
		float yMotion = (entity.fallDistance + 3) / 50f;
		entity.setMotion(0, yMotion, 0);
		entity.lifespan = 6000;
		entity.getPersistentData().remove("FromVoid");
		return false;
	}

}
