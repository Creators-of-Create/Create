package com.simibubi.create.content.legacy;

import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class NoGravMagicalDohickyItem extends Item {

	public NoGravMagicalDohickyItem(Properties p_i48487_1_) {
		super(p_i48487_1_);
	}

	@Override
	public void fillItemCategory(CreativeModeTab pCategory, NonNullList<ItemStack> pItems) {}

	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
		Level world = entity.level;
		Vec3 pos = entity.position();
		CompoundTag persistentData = entity.getPersistentData();

		if (world.isClientSide) {
			if (world.random.nextFloat() < getIdleParticleChance(entity)) {
				Vec3 ppos = VecHelper.offsetRandomly(pos, world.random, .5f);
				world.addParticle(ParticleTypes.END_ROD, ppos.x, pos.y, ppos.z, 0, -.1f, 0);
			}

			if (entity.isSilent() && !persistentData.getBoolean("PlayEffects")) {
				Vec3 basemotion = new Vec3(0, 1, 0);
				world.addParticle(ParticleTypes.FLASH, pos.x, pos.y, pos.z, 0, 0, 0);
				for (int i = 0; i < 20; i++) {
					Vec3 motion = VecHelper.offsetRandomly(basemotion, world.random, 1);
					world.addParticle(ParticleTypes.WITCH, pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
					world.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
				}
				persistentData.putBoolean("PlayEffects", true);
			}

			return false;
		}

		entity.setNoGravity(true);

		if (!persistentData.contains("JustCreated"))
			return false;
		onCreated(entity, persistentData);
		return false;
	}

	protected float getIdleParticleChance(ItemEntity entity) {
		return Mth.clamp(entity.getItem()
			.getCount() - 10, 5, 100) / 64f;
	}

	protected void onCreated(ItemEntity entity, CompoundTag persistentData) {
		entity.lifespan = 6000;
		persistentData.remove("JustCreated");

		// just a flag to tell the client to play an effect
		entity.setSilent(true);
	}

}
