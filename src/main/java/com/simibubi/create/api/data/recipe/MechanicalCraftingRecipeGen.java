package com.simibubi.create.api.data.recipe;

import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import com.google.common.base.Supplier;
import com.simibubi.create.Create;

import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

/**
 * The base class for Mechanical Crafting recipe generation.
 * Addons should extend this and use the {@link #create(Supplier)} method to
 * make recipes.
 * For an example of how you might do this, see Create's implementation: {@link com.simibubi.create.foundation.data.recipe.CreateMechanicalCraftingRecipeGen}.
 * Needs to be added to a registered recipe provider to do anything, see {@link com.simibubi.create.foundation.data.recipe.CreateRecipeProvider}
 */
public abstract class MechanicalCraftingRecipeGen extends BaseRecipeProvider {

	public MechanicalCraftingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String defaultNamespace) {
		super(output, registries, defaultNamespace);
	}

	protected GeneratedRecipeBuilder create(Supplier<ItemLike> result) {
		return new GeneratedRecipeBuilder(result);
	}

	@Override
	public void buildRecipes(RecipeOutput output) {
		all.forEach(c -> c.register(output));
		Create.LOGGER.info("{} registered {} recipe{}", getName(), all.size(), all.size() == 1 ? "" : "s");
	}

	protected class GeneratedRecipeBuilder {

		private String suffix;
		private final Supplier<ItemLike> result;
		private int amount;

		public GeneratedRecipeBuilder(Supplier<ItemLike> result) {
			this.suffix = "";
			this.result = result;
			this.amount = 1;
		}

		public GeneratedRecipeBuilder returns(int amount) {
			this.amount = amount;
			return this;
		}

		public GeneratedRecipeBuilder withSuffix(String suffix) {
			this.suffix = suffix;
			return this;
		}

		public GeneratedRecipe recipe(UnaryOperator<MechanicalCraftingRecipeBuilder> builder) {
			return register(consumer -> {
				MechanicalCraftingRecipeBuilder b =
					builder.apply(MechanicalCraftingRecipeBuilder.shapedRecipe(result.get(), amount));
				ResourceLocation location = asResource("mechanical_crafting/" + RegisteredObjectsHelper.getKeyOrThrow(result.get()
								.asItem())
					.getPath() + suffix);
				b.build(consumer, location);
			});
		}
	}

	@Override
	public String getName() {
		return modid + "'s mechanical crafting recipes";
	}

}
