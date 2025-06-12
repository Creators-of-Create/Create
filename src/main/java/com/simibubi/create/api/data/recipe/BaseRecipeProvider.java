package com.simibubi.create.api.data.recipe;

import com.simibubi.create.Create;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A class containing some basic setup for other recipe generators to use.
 * Addons should extend this if they add a custom recipe type that is not
 * a processing recipe type and want to use Create's helpers.
 * For processing recipes extend {@link ProcessingRecipeGen}.
 */
public abstract class BaseRecipeProvider extends RecipeProvider {
	protected final String modid;
	protected final List<GeneratedRecipe> all = new ArrayList<>();

	public BaseRecipeProvider(PackOutput output, String defaultNamespace) {
		super(output);
		this.modid = defaultNamespace;
	}

	protected ResourceLocation asResource(String path) {
		return new ResourceLocation(modid, path);
	}

	protected GeneratedRecipe register(GeneratedRecipe recipe) {
		all.add(recipe);
		return recipe;
	}

	@Override
	protected void buildRecipes(Consumer<FinishedRecipe> p_200404_1_) {
		all.forEach(c -> c.register(p_200404_1_));
		Create.LOGGER.info(getName() + " registered " + all.size() + " recipe" + (all.size() == 1 ? "" : "s"));
	}

	@FunctionalInterface
	public interface GeneratedRecipe {
		void register(Consumer<FinishedRecipe> consumer);
	}
}
