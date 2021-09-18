package com.simibubi.create.content.curiosities;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nullable;

public class CombustibleItem extends Item {
	private int burnTime = -1;

	public CombustibleItem(Properties properties) {
		super(properties);
	}

	public void setBurnTime(int burnTime) {
		this.burnTime = burnTime;
	}

	@Override
	public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
		return this.burnTime;
	}
}
