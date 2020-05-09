package com.simibubi.create.modules.curiosities.tools;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;

public class BlazingSwordItem extends SwordItem {

	public BlazingSwordItem(IItemTier tier, int attackDamageIn, float attackSpeedIn, Properties builder) {
		super(tier, attackDamageIn, attackSpeedIn, builder);
	}

	@Override
	public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		target.setFire(2);
		return BlazingToolItem.shouldTakeDamage(attacker.world, stack) ? super.hitEntity(stack, target, attacker)
				: true;
	}
	
	@Override
	public int getBurnTime(ItemStack itemStack) {
		return itemStack.getMaxDamage() - itemStack.getDamage() + 1;
	}

}
