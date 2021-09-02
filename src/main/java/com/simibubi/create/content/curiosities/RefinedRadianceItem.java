package com.simibubi.create.content.curiosities;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class RefinedRadianceItem extends NoGravMagicalDohickyItem {

	public RefinedRadianceItem(Properties properties) {
		super(properties);
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}

	@Override
	protected void onCreated(ItemEntity entity, CompoundNBT persistentData) {
		super.onCreated(entity, persistentData);
		entity.setDeltaMovement(entity.getDeltaMovement()
			.add(0, .25f, 0));
	}

}
