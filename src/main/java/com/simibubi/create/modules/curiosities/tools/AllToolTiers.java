package com.simibubi.create.modules.curiosities.tools;

import java.util.function.Supplier;

import com.simibubi.create.AllItems;

import net.minecraft.item.IItemTier;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.LazyLoadBase;

public enum AllToolTiers implements IItemTier {

//	BLAZING(3, 750, 10.0F, 2.5F, 16, () -> {
//		return Ingredient.fromItems(AllItems.BLAZE_BRASS_CUBE.item);
//	}),

	ROSE_QUARTZ(3, 1644, 7.0F, 2.0F, 24, () -> {
		return Ingredient.fromItems(AllItems.POLISHED_ROSE_QUARTZ.item);
	}),

	SHADOW_STEEL(4, 2303, 16.0F, 3.5F, 10, () -> {
		return Ingredient.fromItems(AllItems.SHADOW_STEEL.item);
	}),

	RADIANT(4, 2303, 16.0F, 3.5F, 10, () -> {
		return Ingredient.fromItems(AllItems.REFINED_RADIANCE.item);
	}),

	;
	
	/* used to belong to AllItems. Banished here until harvest events exist */
	
//	BLAZING_PICKAXE(new BlazingToolItem(1, -2.8F, standardProperties(), PICKAXE)),
//	BLAZING_SHOVEL(new BlazingToolItem(1.5F, -3.0F, standardProperties(), SHOVEL)),
//	BLAZING_AXE(new BlazingToolItem(5.0F, -3.0F, standardProperties(), AXE)),
//	BLAZING_SWORD(new BlazingToolItem(3, -2.4F, standardProperties(), SWORD)),
//	
//	ROSE_QUARTZ_PICKAXE(new RoseQuartzToolItem(1, -2.8F, standardProperties(), PICKAXE)),
//	ROSE_QUARTZ_SHOVEL(new RoseQuartzToolItem(1.5F, -3.0F, standardProperties(), SHOVEL)),
//	ROSE_QUARTZ_AXE(new RoseQuartzToolItem(5.0F, -3.0F, standardProperties(), AXE)),
//	ROSE_QUARTZ_SWORD(new RoseQuartzToolItem(3, -2.4F, standardProperties(), SWORD)),
//
//	SHADOW_STEEL_PICKAXE(new ShadowSteelToolItem(2.5F, -2.0F, standardProperties(), PICKAXE)),
//	SHADOW_STEEL_MATTOCK(new ShadowSteelToolItem(2.5F, -1.5F, standardProperties(), SHOVEL, AXE, HOE)),
//	SHADOW_STEEL_SWORD(new ShadowSteelToolItem(3, -2.0F, standardProperties(), SWORD)),

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
