package com.simibubi.create.modules.curiosities.tools;

import java.util.function.Supplier;

import com.simibubi.create.AllItems;

import net.minecraft.item.IItemTier;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.LazyValue;

public enum AllToolTiers implements IItemTier {

	BLAZING(3, 450, 10.0F, 2.5F, 16, () -> {
		return Ingredient.fromItems(Items.BLAZE_ROD);
	}),

	ROSE_QUARTZ(3, 1644, 7.0F, 2.0F, 24, () -> {
		return Ingredient.fromItems(AllItems.POLISHED_ROSE_QUARTZ.get());
	}),

	SHADOW_STEEL(4, 2303, 28.0F, 3.5F, 10, () -> {
		return Ingredient.fromItems(AllItems.SHADOW_STEEL.get());
	}),

	RADIANT(4, 1024, 16.0F, 3.5F, 10, () -> {
		return Ingredient.fromItems(AllItems.REFINED_RADIANCE.get());
	}),

	;
	
	private final int harvestLevel;
	private final int maxUses;
	private final float efficiency;
	private final float attackDamage;
	private final int enchantability;
	private final LazyValue<Ingredient> repairMaterial;

	private AllToolTiers(int harvestLevelIn, int maxUsesIn, float efficiencyIn, float attackDamageIn,
			int enchantabilityIn, Supplier<Ingredient> repairMaterialIn) {
		this.harvestLevel = harvestLevelIn;
		this.maxUses = maxUsesIn;
		this.efficiency = efficiencyIn;
		this.attackDamage = attackDamageIn;
		this.enchantability = enchantabilityIn;
		this.repairMaterial = new LazyValue<>(repairMaterialIn);
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
