package com.simibubi.create.foundation.data.recipe;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags.AllFluidTags;
import com.simibubi.create.Create;
import com.simibubi.create.api.data.recipe.FillingRecipeGen;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluids;

import net.neoforged.neoforge.common.Tags;

/**
 * Create's own Data Generation for Filling recipes
 * @see FillingRecipeGen
 */
@SuppressWarnings("unused")
public final class CreateFillingRecipeGen extends FillingRecipeGen {

	GeneratedRecipe

		HONEY_BOTTLE = create("honey_bottle", b -> b.require(Tags.Fluids.HONEY, 250)
		.require(Items.GLASS_BOTTLE)
		.output(Items.HONEY_BOTTLE)),

	BUILDERS_TEA = create("builders_tea", b -> b.require(AllFluids.TEA.get(), 250)
		.require(Items.GLASS_BOTTLE)
		.output(AllItems.BUILDERS_TEA.get())),

	FD_MILK = create(Mods.FD.recipeId("milk_bottle"), b -> b.require(Tags.Fluids.MILK, 250)
		.require(Items.GLASS_BOTTLE)
		.output(1, Mods.FD, "milk_bottle", 1)
		.whenModLoaded(Mods.FD.getId())),

	BLAZE_CAKE = create("blaze_cake", b -> b.require(Fluids.LAVA, 250)
		.require(AllItems.BLAZE_CAKE_BASE.get())
		.output(AllItems.BLAZE_CAKE.get())),

	HONEYED_APPLE = create("honeyed_apple", b -> b.require(Tags.Fluids.HONEY, 250)
		.require(Items.APPLE)
		.output(AllItems.HONEYED_APPLE.get())),

	SWEET_ROLL = create("sweet_roll", b -> b.require(Tags.Fluids.MILK, 250)
		.require(Items.BREAD)
		.output(AllItems.SWEET_ROLL.get())),

	CHOCOLATE_BERRIES = create("chocolate_glazed_berries", b -> b.require(AllFluids.CHOCOLATE.get(), 250)
		.require(Items.SWEET_BERRIES)
		.output(AllItems.CHOCOLATE_BERRIES.get())),

	GRASS_BLOCK = create("grass_block", b -> b.require(Fluids.WATER, 500)
		.require(Items.DIRT)
		.output(Items.GRASS_BLOCK)),

	GUNPOWDER = create("gunpowder", b -> b.require(PotionFluidHandler.potionIngredient(Potions.HARMING, 25))
		.require(AllItems.CINDER_FLOUR.get())
		.output(Items.GUNPOWDER)),

	REDSTONE = create("redstone", b -> b.require(PotionFluidHandler.potionIngredient(Potions.STRENGTH, 25))
		.require(AllItems.CINDER_FLOUR.get())
		.output(Items.REDSTONE)),

	GLOWSTONE = create("glowstone", b -> b.require(PotionFluidHandler.potionIngredient(Potions.NIGHT_VISION, 25))
		.require(AllItems.CINDER_FLOUR.get())
		.output(Items.GLOWSTONE_DUST)),


	AM_LAVA = create(Mods.AM.recipeId("lava_bottle"), b -> b.require(Fluids.LAVA, 250)
		.require(Items.GLASS_BOTTLE)
		.output(1, Mods.AM, "lava_bottle", 1)
		.whenModLoaded(Mods.AM.getId())),

	BWG_LUSH_GRASS = create(Mods.BWG.recipeId("lush_grass_block"), b -> b.require(Mods.BWG, "lush_dirt")
		.require(Fluids.WATER, 500)
		.output(Mods.BWG, "lush_grass_block")
		.whenModLoaded(Mods.BWG.getId())),

	NEA_MILK = create(Mods.NEA.recipeId("milk_bottle"), b -> b.require(Tags.Fluids.MILK, 250)
		.require(Items.GLASS_BOTTLE)
		.output(1, Mods.NEA, "milk_bottle", 1)
		.whenModLoaded(Mods.NEA.getId())),

	AET_GRASS = moddedGrass(Mods.AET, "aether"),

	RU_PEAT_GRAS = moddedGrass(Mods.RU, "peat"),

	RU_SILT_GRAS = moddedGrass(Mods.RU, "silt"),

	// Vampirism

	VMP_CURSED_GRASS = create(Mods.VMP.recipeId("cursed_grass"), b -> b.require(Fluids.WATER, 500)
		.require(Mods.VMP, "cursed_earth")
		.output(Mods.VMP, "cursed_grass")
		.whenModLoaded(Mods.VMP.getId())),

	// IE

	IE_TREATED_WOOD = create(Mods.IE.recipeId("treated_wood_in_spout"),
		b -> b.require(AllFluidTags.CREOSOTE.tag, 125)
			.require(CreateRecipeProvider.I.planks())
			.output(Mods.IE, "treated_wood_horizontal")
			.whenModLoaded(Mods.IE.getId()));


	public CreateFillingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, Create.ID);
	}
}
