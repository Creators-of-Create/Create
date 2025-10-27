package com.simibubi.create.foundation.data.recipe;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.api.data.recipe.EmptyingRecipeGen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

import net.neoforged.neoforge.common.NeoForgeMod;

/**
 * Create's own Data Generation for Emptying recipes
 * @see EmptyingRecipeGen
 */
@SuppressWarnings("unused")
public final class CreateEmptyingRecipeGen extends EmptyingRecipeGen {

	/*
	 * potion/water bottles are handled internally
	 */

	GeneratedRecipe

	HONEY_BOTTLE = create("honey_bottle", b -> b.require(Items.HONEY_BOTTLE)
		.output(AllFluids.HONEY.get(), 250)
		.output(Items.GLASS_BOTTLE)),

	BUILDERS_TEA = create("builders_tea", b -> b.require(AllItems.BUILDERS_TEA.get())
		.output(AllFluids.TEA.get(), 250)
		.output(Items.GLASS_BOTTLE)),

	FD_MILK = create(Mods.FD.recipeId("milk_bottle"), b -> b.require(Mods.FD, "milk_bottle")
		.output(NeoForgeMod.MILK.get(), 250)
		.output(Items.GLASS_BOTTLE)
		.whenModLoaded(Mods.FD.getId())),

	AM_LAVA = create(Mods.AM.recipeId("lava_bottle"), b -> b.require(Mods.AM, "lava_bottle")
		.output(Items.GLASS_BOTTLE)
		.output(Fluids.LAVA, 250)
		.whenModLoaded(Mods.AM.getId())),

	NEO_MILK = create(Mods.NEA.recipeId("milk_bottle"), b -> b.require(Mods.FD, "milk_bottle")
		.output(NeoForgeMod.MILK.get(), 250)
		.output(Items.GLASS_BOTTLE)
		.whenModLoaded(Mods.NEA.getId()))

	;

	public CreateEmptyingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, Create.ID);
	}
}
