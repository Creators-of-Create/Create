package com.simibubi.create.foundation.data.recipe;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.api.data.recipe.MechanicalCraftingRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider.I;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.crafting.Ingredient;

import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.Tags.Items;

/**
 * Create's own Data Generation for Mechanical Crafting recipes
 * @see MechanicalCraftingRecipeGen
 */
@SuppressWarnings("unused")
public final class CreateMechanicalCraftingRecipeGen extends MechanicalCraftingRecipeGen {

	GeneratedRecipe

	CRUSHING_WHEEL = create(AllBlocks.CRUSHING_WHEEL::get).returns(2)
		.recipe(b -> b.key('P', I.tag(ItemTags.PLANKS))
			.key('S', I.tag(I.stone()))
			.key('A', I.andesiteAlloy())
			.patternLine(" AAA ")
			.patternLine("AAPAA")
			.patternLine("APSPA")
			.patternLine("AAPAA")
			.patternLine(" AAA ")
			.disallowMirrored()),

	WAND_OF_SYMMETRY =
		create(AllItems.WAND_OF_SYMMETRY::get).recipe(b -> b.key('E', I.tag(Tags.Items.ENDER_PEARLS))
			.key('G', I.tag(Items.GLASS_BLOCKS))
			.key('P', I.precisionMechanism())
			.key('O', I.tag(Items.OBSIDIANS))
			.key('B', I.tag(I.brass()))
			.patternLine(" G ")
			.patternLine("GEG")
			.patternLine(" P ")
			.patternLine(" B ")
			.patternLine(" O ")),

	EXTENDO_GRIP = create(AllItems.EXTENDO_GRIP::get).returns(1)
		.recipe(b -> b.key('L', I.tag(I.brass()))
			.key('R', I.precisionMechanism())
			.key('H', AllItems.BRASS_HAND.get())
			.key('S', I.tag(Tags.Items.RODS_WOODEN))
			.patternLine(" L ")
			.patternLine(" R ")
			.patternLine("SSS")
			.patternLine("SSS")
			.patternLine(" H ")
			.disallowMirrored()),

	POTATO_CANNON = create(AllItems.POTATO_CANNON::get).returns(1)
		.recipe(b -> b.key('L', I.andesiteAlloy())
			.key('R', I.precisionMechanism())
			.key('S', AllBlocks.FLUID_PIPE.get())
			.key('C', I.tag(I.copper()))
			.patternLine("LRSSS")
			.patternLine("CC   "))

	;


	public CreateMechanicalCraftingRecipeGen(PackOutput output, CompletableFuture<Provider> registries) {
		super(output, registries, Create.ID);
	}
}
