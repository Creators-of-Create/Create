package com.simibubi.create.foundation.data.recipe;

import java.util.function.UnaryOperator;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.contraptions.itemAssembly.SequencedAssemblyRecipeBuilder;

import net.minecraft.data.DataGenerator;

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
	
	CLOCKWORK_COMPONENT = create("clockwork_component", b -> b.require(I.goldSheet())
		.transitionTo(AllItems.INCOMPLETE_CLOCKWORK_COMPONENT.get())
		.addOutput(AllItems.CLOCKWORK_COMPONENT.get(), 12)
		.addOutput(AllItems.GOLDEN_SHEET.get(), 2)
		.addOutput(AllItems.ANDESITE_ALLOY.get(), 2)
		.addOutput(AllBlocks.COGWHEEL.get(), 1) //TODO add more junk
		.loops(5)
		.addStep(DeployerApplicationRecipe::new, rb -> rb.require(I.cog()))
		.addStep(DeployerApplicationRecipe::new, rb -> rb.require(I.largeCog()))
		.addStep(DeployerApplicationRecipe::new, rb -> rb.require(I.ironNugget()))
		)
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
