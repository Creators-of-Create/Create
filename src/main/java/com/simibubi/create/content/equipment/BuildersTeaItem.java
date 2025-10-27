package com.simibubi.create.content.equipment;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class BuildersTeaItem extends Item {
	public BuildersTeaItem(Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
		ItemStack eatResult = super.finishUsingItem(stack, level, livingEntity);
		if (livingEntity instanceof Player player && !player.getAbilities().instabuild) {
			if (eatResult.isEmpty()) {
				return Items.GLASS_BOTTLE.getDefaultInstance();
			} else {
				player.getInventory().add(Items.GLASS_BOTTLE.getDefaultInstance());
			}
		}
		return eatResult;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 42;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.DRINK;
	}
}
