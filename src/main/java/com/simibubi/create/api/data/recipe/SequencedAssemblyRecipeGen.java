package com.simibubi.create.api.data.recipe;

import java.util.function.UnaryOperator;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipeBuilder;

import net.minecraft.data.PackOutput;

public abstract class SequencedAssemblyRecipeGen extends BaseRecipeProvider {

	public SequencedAssemblyRecipeGen(PackOutput output, String defaultNamespace) {
		super(output, defaultNamespace);
	}

	@Override
	public String getName() {
		return modid + "'s sequenced assembly recipes";
	}

	protected GeneratedRecipe create(String name, UnaryOperator<SequencedAssemblyRecipeBuilder> transform) {
		GeneratedRecipe generatedRecipe =
			c -> transform.apply(new SequencedAssemblyRecipeBuilder(asResource(name)))
				.build(c);
		all.add(generatedRecipe);
		return generatedRecipe;
	}
}
