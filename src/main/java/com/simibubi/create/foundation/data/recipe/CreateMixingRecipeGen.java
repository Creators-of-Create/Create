package com.simibubi.create.foundation.data.recipe;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.Create;
import com.simibubi.create.api.data.recipe.MixingRecipeGen;
import com.simibubi.create.content.processing.recipe.HeatCondition;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.crafting.BlockTagIngredient;

/**
 * Create's own Data Generation for Mixing recipes
 * @see MixingRecipeGen
 */
@SuppressWarnings("unused")
public final class CreateMixingRecipeGen extends MixingRecipeGen {

	GeneratedRecipe

		TEMP_LAVA = create("lava_from_cobble", b -> b.require(Tags.Items.COBBLESTONES)
		.output(Fluids.LAVA, 50)
		.requiresHeat(HeatCondition.SUPERHEATED)),

	TEA = create("tea", b -> b.require(Fluids.WATER, 250)
		.require(Tags.Fluids.MILK, 250)
		.require(ItemTags.LEAVES)
		.output(AllFluids.TEA.get(), 500)
		.requiresHeat(HeatCondition.HEATED)),

	CHOCOLATE = create("chocolate", b -> b.require(Tags.Fluids.MILK, 250)
		.require(Items.SUGAR)
		.require(Items.COCOA_BEANS)
		.output(AllFluids.CHOCOLATE.get(), 250)
		.requiresHeat(HeatCondition.HEATED)),

	CHOCOLATE_MELTING = create("chocolate_melting", b -> b.require(AllItems.BAR_OF_CHOCOLATE.get())
		.output(AllFluids.CHOCOLATE.get(), 250)
		.requiresHeat(HeatCondition.HEATED)),

	HONEY = create("honey", b -> b.require(Items.HONEY_BLOCK)
		.output(AllFluids.HONEY.get(), 1000)
		.requiresHeat(HeatCondition.HEATED)),

	DOUGH = create("dough_by_mixing", b -> b.require(CreateRecipeProvider.I.wheatFlour())
		.require(Fluids.WATER, 1000)
		.output(AllItems.DOUGH.get(), 1)),

	BRASS_INGOT = create("brass_ingot", b -> b.require(CreateRecipeProvider.I.copper())
		.require(CreateRecipeProvider.I.zinc())
		.output(AllItems.BRASS_INGOT.get(), 2)
		.requiresHeat(HeatCondition.HEATED)),

	ANDESITE_ALLOY = create("andesite_alloy", b -> b.require(Blocks.ANDESITE)
		.require(CreateRecipeProvider.I.ironNugget())
		.output(CreateRecipeProvider.I.andesiteAlloy(), 1)),

	ANDESITE_ALLOY_FROM_ZINC = create("andesite_alloy_from_zinc", b -> b.require(Blocks.ANDESITE)
		.require(CreateRecipeProvider.I.zincNugget())
		.output(CreateRecipeProvider.I.andesiteAlloy(), 1)),

	MUD = create("mud_by_mixing", b -> b.require(new BlockTagIngredient(BlockTags.CONVERTABLE_TO_MUD))
		.require(Fluids.WATER, 250)
		.output(Blocks.MUD, 1)),

	PULP = create("cardboard_pulp", b -> b
		.require(AllItemTags.PULPIFIABLE.tag)
		.require(AllItemTags.PULPIFIABLE.tag)
		.require(AllItemTags.PULPIFIABLE.tag)
		.require(AllItemTags.PULPIFIABLE.tag)
		.require(Fluids.WATER, 250)
		.output(AllItems.PULP, 1)),

	// AE2

	AE2_FLUIX = create(Mods.AE2.recipeId("fluix_crystal"), b -> b.require(Tags.Items.DUSTS_REDSTONE)
		.require(Fluids.WATER, 250)
		.require(Mods.AE2, "charged_certus_quartz_crystal")
		.require(Tags.Items.GEMS_QUARTZ)
		.output(1f, Mods.AE2, "fluix_crystal", 2)
		.whenModLoaded(Mods.AE2.getId())),

	// Regions Unexplored

	RU_PEAT_MUD = moddedMud(Mods.RU, "peat"),
	RU_SILT_MUD = moddedMud(Mods.RU, "silt")

	;

	public CreateMixingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, Create.ID);
	}
}
