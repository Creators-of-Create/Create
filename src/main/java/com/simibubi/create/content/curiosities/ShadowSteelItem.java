package com.simibubi.create.content.curiosities;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;

public class ShadowSteelItem extends NoGravMagicalDohickyItem {

	public ShadowSteelItem(Properties properties) {
		super(properties);
	}

	@Override
	protected void onCreated(ItemEntity entity, CompoundNBT persistentData) {
		super.onCreated(entity, persistentData);
		float yMotion = (entity.fallDistance + 3) / 50f;
		entity.setMotion(0, yMotion, 0);
	}
	
	@Override
	protected float getIdleParticleChance(ItemEntity entity) {
		return (float) (MathHelper.clamp(entity.getItem()
			.getCount() - 10, MathHelper.clamp(entity.getMotion().y * 20, 5, 20), 100) / 64f);
	}

}
