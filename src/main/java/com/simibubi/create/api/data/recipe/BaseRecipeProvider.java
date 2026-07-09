package com.simibubi.create.api.data.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.simibubi.create.Create;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/**
 * A class containing some basic setup for other recipe generators to use.
 * Addons should extend this if they add a custom recipe type that is not
 * a processing recipe type and want to use Create's helpers.
 * For processing recipes extend {@link StandardProcessingRecipeGen}.
 */
public abstract class BaseRecipeProvider extends RecipeProvider.Runner {
	protected final String modid;
	protected final List<GeneratedRecipe> all = new ArrayList<>();
	private ActiveProvider activeProvider;

	public BaseRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String defaultNamespace) {
		super(output, registries);
		this.modid = defaultNamespace;
	}

	protected Identifier asResource(String path) {
		return Identifier.fromNamespaceAndPath(modid, path);
	}

	protected GeneratedRecipe register(GeneratedRecipe recipe) {
		all.add(recipe);
		return recipe;
	}

	@Override
	protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
		return new ActiveProvider(registries, output) {
			@Override
			protected void buildRecipes() {
				activeProvider = this;
				try {
					BaseRecipeProvider.this.buildRecipes(output);
				} finally {
					activeProvider = null;
				}
			}
		};
	}

	protected void buildRecipes(RecipeOutput recipeOutput) {
		all.forEach(c -> c.register(recipeOutput));
		Create.LOGGER.info("{} registered {} recipe{}", getName(), all.size(), all.size() == 1 ? "" : "s");
	}

	protected ShapedRecipeBuilder shaped(RecipeCategory category, ItemStackTemplate stack) {
		return provider().shapedDelegate(category, stack);
	}

	protected ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike item) {
		return provider().shapedDelegate(category, item);
	}

	protected ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike item, int count) {
		return provider().shapedDelegate(category, item, count);
	}

	protected ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemStackTemplate stack) {
		return provider().shapelessDelegate(category, stack);
	}

	protected ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike item) {
		return provider().shapelessDelegate(category, item);
	}

	protected ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike item, int count) {
		return provider().shapelessDelegate(category, item, count);
	}

	protected Ingredient tag(TagKey<Item> id) {
		return provider().tagDelegate(id);
	}

	protected HolderGetter<Item> itemLookup() {
		return provider().itemLookupDelegate();
	}

	private ActiveProvider provider() {
		if (activeProvider == null)
			throw new IllegalStateException("Recipe helpers can only be used while recipes are being generated");
		return activeProvider;
	}

	private static abstract class ActiveProvider extends RecipeProvider {
		protected ActiveProvider(HolderLookup.Provider registries, RecipeOutput output) {
			super(registries, output);
		}

		ShapedRecipeBuilder shapedDelegate(RecipeCategory category, ItemStackTemplate stack) {
			return shaped(category, stack);
		}

		ShapedRecipeBuilder shapedDelegate(RecipeCategory category, ItemLike item) {
			return shaped(category, item);
		}

		ShapedRecipeBuilder shapedDelegate(RecipeCategory category, ItemLike item, int count) {
			return shaped(category, item, count);
		}

		ShapelessRecipeBuilder shapelessDelegate(RecipeCategory category, ItemStackTemplate stack) {
			return shapeless(category, stack);
		}

		ShapelessRecipeBuilder shapelessDelegate(RecipeCategory category, ItemLike item) {
			return shapeless(category, item);
		}

		ShapelessRecipeBuilder shapelessDelegate(RecipeCategory category, ItemLike item, int count) {
			return shapeless(category, item, count);
		}

		Ingredient tagDelegate(TagKey<Item> id) {
			return tag(id);
		}

		HolderGetter<Item> itemLookupDelegate() {
			return items;
		}
	}

	@FunctionalInterface
	public interface GeneratedRecipe {
		void register(RecipeOutput recipeOutput);
	}
}
