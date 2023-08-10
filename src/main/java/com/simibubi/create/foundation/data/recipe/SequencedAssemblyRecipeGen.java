package com.simibubi.create.foundation.data.recipe;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.Create;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipeBuilder;

import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;

public class SequencedAssemblyRecipeGen extends CreateRecipeProvider {

	GeneratedRecipe

//	TEST = create("test", b -> b.require(I.goldSheet())
//		.transitionTo(AllItems.INCOMPLETE_CLOCKWORK_COMPONENT.get())
//		.addOutput(AllItems.CLOCKWORK_COMPONENT.get(), 10)
//		.addOutput(AllItems.GOLDEN_SHEET.get(), 5)
//		.addOutput(AllItems.ANDESITE_ALLOY.get(), 2)
//		.addOutput(AllBlocks.COGWHEEL.get(), 1)
//		.loops(5)
//		.addStep(DeployerApplicationRecipe::new, rb -> rb.require(I.cog()))
//		.addStep(PressingRecipe::new, rb -> rb)
//		.addStep(FillingRecipe::new, rb -> rb.require(Fluids.LAVA, 500))
//		.addStep(CuttingRecipe::new, rb -> rb.averageProcessingDuration())
//		.addStep(FillingRecipe::new, rb -> rb.require(Fluids.WATER, 100))
//		)

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

		REINFORCED_SHEET = create("sturdy_sheet", b -> b.require(AllItems.POWDERED_OBSIDIAN.get())
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

	public SequencedAssemblyRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	protected GeneratedRecipe create(String name, UnaryOperator<SequencedAssemblyRecipeBuilder> transform) {
		GeneratedRecipe generatedRecipe =
			c -> transform.apply(new SequencedAssemblyRecipeBuilder(Create.asResource(name)))
				.build(c);
		all.add(generatedRecipe);
		return generatedRecipe;
	}

	@Override
	public String getName() {
		return "Create's Sequenced Assembly Recipes";
	}

}
