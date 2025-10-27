package com.simibubi.create.foundation.data.recipe;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.Create;
import com.simibubi.create.api.data.recipe.SequencedAssemblyRecipeGen;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider.I;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;

/**
 * Create's own Data Generation for Sequenced Assembly recipes
 * @see SequencedAssemblyRecipeGen
 */
@SuppressWarnings("unused")
public final class CreateSequencedAssemblyRecipeGen extends SequencedAssemblyRecipeGen {

	GeneratedRecipe

	PRECISION_MECHANISM = create("precision_mechanism", b -> b.require(I.goldSheet())
		.transitionTo(AllItems.INCOMPLETE_PRECISION_MECHANISM.get())
		.addOutput(AllItems.PRECISION_MECHANISM.get(), 120)
		.addOutput(AllItems.GOLDEN_SHEET.get(), 8)
		.addOutput(AllItems.ANDESITE_ALLOY.get(), 8)
		.addOutput(AllBlocks.COGWHEEL.get(), 5)
		.addOutput(Items.GOLD_NUGGET, 3)
		.addOutput(AllBlocks.SHAFT.get(), 2)
		.addOutput(AllItems.CRUSHED_GOLD.get(), 2)
		.addOutput(Items.IRON_INGOT, 1)
		.addOutput(Items.CLOCK, 1)
		.loops(5)
		.addStep(DeployerApplicationRecipe::new, rb -> rb.require(I.cog()))
		.addStep(DeployerApplicationRecipe::new, rb -> rb.require(I.largeCog()))
		.addStep(DeployerApplicationRecipe::new, rb -> rb.require(I.ironNugget()))),

	REINFORCED_SHEET = create("sturdy_sheet", b -> b.require(AllItemTags.OBSIDIAN_DUST.tag)
		.transitionTo(AllItems.INCOMPLETE_REINFORCED_SHEET.get())
		.addOutput(AllItems.STURDY_SHEET.get(), 1)
		.loops(1)
		.addStep(FillingRecipe::new, rb -> rb.require(Fluids.LAVA, 500))
		.addStep(PressingRecipe::new, rb -> rb)
		.addStep(PressingRecipe::new, rb -> rb)),

	TRACK = create("track", b -> b.require(AllItemTags.SLEEPERS.tag)
		.transitionTo(AllItems.INCOMPLETE_TRACK.get())
		.addOutput(AllBlocks.TRACK.get(), 1)
		.loops(1)
		.addStep(DeployerApplicationRecipe::new,
			rb -> rb.require(Ingredient.fromValues(
				Stream.of(new Ingredient.TagValue(I.ironNugget()), new Ingredient.TagValue(I.zincNugget())))))
		.addStep(DeployerApplicationRecipe::new,
			rb -> rb.require(Ingredient.fromValues(
				Stream.of(new Ingredient.TagValue(I.ironNugget()), new Ingredient.TagValue(I.zincNugget())))))
		.addStep(PressingRecipe::new, rb -> rb))

		;

	public CreateSequencedAssemblyRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, Create.ID);
	}
}
