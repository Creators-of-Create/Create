package com.simibubi.create.foundation.data.recipe;

import java.util.function.UnaryOperator;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.contraptions.components.press.PressingRecipe;
import com.simibubi.create.content.contraptions.components.saw.CuttingRecipe;
import com.simibubi.create.content.contraptions.fluids.actors.FillingRecipe;
import com.simibubi.create.content.contraptions.itemAssembly.SequencedAssemblyRecipeBuilder;

import net.minecraft.data.DataGenerator;
import net.minecraft.fluid.Fluids;

public class SequencedAssemblyRecipeGen extends CreateRecipeProvider {

	GeneratedRecipe

	TEST = create("test", b -> b.require(I.goldSheet())
		.transitionTo(AllItems.INCOMPLETE_CLOCKWORK_ELEMENT.get())
		.addOutput(AllItems.CLOCKWORK_ELEMENT.get(), 10)
		.addOutput(AllItems.GOLDEN_SHEET.get(), 5)
		.addOutput(AllItems.ANDESITE_ALLOY.get(), 2)
		.addOutput(AllBlocks.COGWHEEL.get(), 1)
		.expectedSteps(20)
		.addStep(DeployerApplicationRecipe::new, rb -> rb.require(I.cog()))
		.addStep(PressingRecipe::new, rb -> rb)
		.addStep(DeployerApplicationRecipe::new, rb -> rb.require(I.largeCog()))
		.addStep(FillingRecipe::new, rb -> rb.require(Fluids.WATER, 100))
		.addStep(DeployerApplicationRecipe::new, rb -> rb.require(I.shaft()))
		.addStep(CuttingRecipe::new, rb -> rb.averageProcessingDuration()))
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
