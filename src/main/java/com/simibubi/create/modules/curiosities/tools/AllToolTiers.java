package com.simibubi.create.modules.curiosities.tools;

import java.util.function.Supplier;

import com.simibubi.create.AllItems;

import net.minecraft.item.IItemTier;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.LazyLoadBase;

public enum AllToolTiers implements IItemTier {

	BLAZING(3, 750, 10.0F, 2.5F, 16, () -> {
		return Ingredient.fromItems(AllItems.BLAZE_BRASS_CUBE.item);
	}),

	ROSE_QUARTZ(3, 1644, 7.0F, 2.0F, 24, () -> {
		return Ingredient.fromItems(AllItems.REFINED_ROSE_QUARTZ.item);
	}),

	SHADOW_STEEL(4, 2303, 16.0F, 3.5F, 10, () -> {
		return Ingredient.fromItems(AllItems.SHADOW_STEEL_CUBE.item);
	}),

	RADIANT(4, 2303, 16.0F, 3.5F, 10, () -> {
		return Ingredient.fromItems(AllItems.REFINED_RADIANCE_CUBE.item);
	}),

	;

	private final int harvestLevel;
	private final int maxUses;
	private final float efficiency;
	private final float attackDamage;
	private final int enchantability;
	private final LazyLoadBase<Ingredient> repairMaterial;

	private AllToolTiers(int harvestLevelIn, int maxUsesIn, float efficiencyIn, float attackDamageIn,
			int enchantabilityIn, Supplier<Ingredient> repairMaterialIn) {
		this.harvestLevel = harvestLevelIn;
		this.maxUses = maxUsesIn;
		this.efficiency = efficiencyIn;
		this.attackDamage = attackDamageIn;
		this.enchantability = enchantabilityIn;
		this.repairMaterial = new LazyLoadBase<>(repairMaterialIn);
	}

	public int getMaxUses() {
		return this.maxUses;
	}

	public float getEfficiency() {
		return this.efficiency;
	}

	public float getAttackDamage() {
		return this.attackDamage;
	}

	public int getHarvestLevel() {
		return this.harvestLevel;
	}

	public int getEnchantability() {
		return this.enchantability;
	}

	public Ingredient getRepairMaterial() {
		return this.repairMaterial.getValue();
	}
}
