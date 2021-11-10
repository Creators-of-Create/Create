package com.simibubi.create.content.curiosities.armor;

import java.util.function.Supplier;

import com.simibubi.create.AllSoundEvents;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum AllArmorMaterials implements ArmorMaterial {

	COPPER("copper", 7, new int[] { 1, 3, 4, 2 }, 25, AllSoundEvents.COPPER_ARMOR_EQUIP.getMainEvent(), 0.0F, 0.0F,
		() -> Ingredient.of(Items.COPPER_INGOT))

	;

	private static final int[] MAX_DAMAGE_ARRAY = new int[] { 13, 15, 16, 11 };
	private final String name;
	private final int maxDamageFactor;
	private final int[] damageReductionAmountArray;
	private final int enchantability;
	private final SoundEvent soundEvent;
	private final float toughness;
	private final float knockbackResistance;
	private final LazyLoadedValue<Ingredient> repairMaterial;

	private AllArmorMaterials(String p_i231593_3_, int p_i231593_4_, int[] p_i231593_5_, int p_i231593_6_,
		SoundEvent p_i231593_7_, float p_i231593_8_, float p_i231593_9_, Supplier<Ingredient> p_i231593_10_) {
		this.name = p_i231593_3_;
		this.maxDamageFactor = p_i231593_4_;
		this.damageReductionAmountArray = p_i231593_5_;
		this.enchantability = p_i231593_6_;
		this.soundEvent = p_i231593_7_;
		this.toughness = p_i231593_8_;
		this.knockbackResistance = p_i231593_9_;
		this.repairMaterial = new LazyLoadedValue<>(p_i231593_10_);
	}

	public int getDurabilityForSlot(EquipmentSlot p_200896_1_) {
		return MAX_DAMAGE_ARRAY[p_200896_1_.getIndex()] * this.maxDamageFactor;
	}

	public int getDefenseForSlot(EquipmentSlot p_200902_1_) {
		return this.damageReductionAmountArray[p_200902_1_.getIndex()];
	}

	public int getEnchantmentValue() {
		return this.enchantability;
	}

	public SoundEvent getEquipSound() {
		return this.soundEvent;
	}

	public Ingredient getRepairIngredient() {
		return this.repairMaterial.get();
	}

	@OnlyIn(Dist.CLIENT)
	public String getName() {
		return this.name;
	}

	public float getToughness() {
		return this.toughness;
	}

	public float getKnockbackResistance() {
		return this.knockbackResistance;
	}

}