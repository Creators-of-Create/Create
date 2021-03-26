package com.simibubi.create.content.curiosities;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class RefinedRadianceItem extends NoGravMagicalDohickyItem {

	public RefinedRadianceItem(Properties properties) {
		super(properties);
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return true;
	}

	@Override
	protected void onCreated(ItemEntity entity, CompoundNBT persistentData) {
		super.onCreated(entity, persistentData);
		entity.setMotion(entity.getMotion()
			.add(0, .15f, 0));
	}

}
