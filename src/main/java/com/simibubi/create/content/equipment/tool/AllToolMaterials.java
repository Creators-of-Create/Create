package com.simibubi.create.content.equipment.tool;

import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.AllItems;
import com.simibubi.create.Create;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

public enum AllToolMaterials implements Tier {
	CARDBOARD(Create.asResource("cardboard")
		.toString(), 0, 1, 2, 1, () -> Ingredient.of(AllItems.CARDBOARD.asItem()))
	;

	public final String name;

	private final int uses;
	private final float speed;
	private final float damageBonus;
	private final int enchantValue;
	private final Supplier<Ingredient> repairMaterial;

	private AllToolMaterials(String name, int uses, float speed, float damageBonus, int enchantValue,
		Supplier<Ingredient> repairMaterial) {
		this.name = name;
		this.uses = uses;
		this.speed = speed;
		this.damageBonus = damageBonus;
		this.enchantValue = enchantValue;
		this.repairMaterial = repairMaterial;
	}

	@Override
	public int getUses() {
		return uses;
	}

	@Override
	public float getSpeed() {
		return speed;
	}

	@Override
	public float getAttackDamageBonus() {
		return damageBonus;
	}

	@Override
	public @NotNull TagKey<Block> getIncorrectBlocksForDrops() {
		return BlockTags.INCORRECT_FOR_WOODEN_TOOL;
	}

	@Override
	public int getEnchantmentValue() {
		return enchantValue;
	}

	@Override
	public @NotNull Ingredient getRepairIngredient() {
		return repairMaterial.get();
	}
}
