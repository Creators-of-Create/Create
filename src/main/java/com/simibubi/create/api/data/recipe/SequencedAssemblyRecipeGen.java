package com.simibubi.create.api.data.recipe;

import java.util.function.UnaryOperator;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipeBuilder;

import net.minecraft.data.PackOutput;

/**
 * The base class for Sequenced Assembly recipe generation.
 * Addons should extend this and use {@link #create(String, UnaryOperator)} to modify builders
 * to create recipes.
 * For an example of how you might do this, see Create's implementation: {@link com.simibubi.create.foundation.data.recipe.CreateSequencedAssemblyRecipeGen}.
 * Needs to be added to a registered recipe provider to do anything, see {@link com.simibubi.create.foundation.data.recipe.CreateRecipeProvider}
 */
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
