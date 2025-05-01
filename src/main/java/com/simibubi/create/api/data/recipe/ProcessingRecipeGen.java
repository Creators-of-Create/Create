package com.simibubi.create.api.data.recipe;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import org.jetbrains.annotations.NotNull;

/**
 * A base class for all processing recipes, containing helper methods
 * for datagenning processing recipes. Addons should extend this for
 * custom processing recipe types, and return that recipe type in {@link #getRecipeType()}.
 */
public abstract class ProcessingRecipeGen extends BaseRecipeProvider {

	public ProcessingRecipeGen(PackOutput generator, String defaultNamespace) {
		super(generator, defaultNamespace);
	}

	/**
	 * Create a processing recipe with a single itemstack ingredient, using its id
	 * as the name of the recipe
	 */
	protected <T extends ProcessingRecipe<?>> GeneratedRecipe create(String namespace,
																								Supplier<ItemLike> singleIngredient, UnaryOperator<ProcessingRecipeBuilder<T>> transform) {
		ProcessingRecipeSerializer<T> serializer = getSerializer();
		GeneratedRecipe generatedRecipe = c -> {
			ItemLike itemLike = singleIngredient.get();
			transform
				.apply(new ProcessingRecipeBuilder<>(serializer.getFactory(),
					new ResourceLocation(namespace, CatnipServices.REGISTRIES.getKeyOrThrow(itemLike.asItem())
						.getPath())).withItemIngredients(Ingredient.of(itemLike)))
				.build(c);
		};
		all.add(generatedRecipe);
		return generatedRecipe;
	}

	/**
	 * Create a new processing recipe, with supplied name and recipe definitions
	 * provided by the function
	 */
	protected <T extends ProcessingRecipe<?>> GeneratedRecipe createWithDeferredId(Supplier<ResourceLocation> name,
																											  UnaryOperator<ProcessingRecipeBuilder<T>> transform) {
		ProcessingRecipeSerializer<T> serializer = getSerializer();
		GeneratedRecipe generatedRecipe =
			c -> transform.apply(new ProcessingRecipeBuilder<>(serializer.getFactory(), name.get()))
				.build(c);
		all.add(generatedRecipe);
		return generatedRecipe;
	}

	/**
	 * Create a new processing recipe, with recipe definitions provided by the
	 * function
	 */
	protected <T extends ProcessingRecipe<?>> GeneratedRecipe create(ResourceLocation name,
																								UnaryOperator<ProcessingRecipeBuilder<T>> transform) {
		return createWithDeferredId(() -> name, transform);
	}

	/**
	 * Gets this recipe generators generated recipe type.
	 * Subclasses should override this to return an instance of IRecipeTypeInfo
	 * Create uses an enum, however this is not in any way required for addons.
	 */
	protected abstract IRecipeTypeInfo getRecipeType();

	protected <T extends ProcessingRecipe<?>> ProcessingRecipeSerializer<T> getSerializer() {
		return getRecipeType().getSerializer();
	}

	protected Supplier<ResourceLocation> idWithSuffix(Supplier<ItemLike> item, String suffix) {
		return () -> {
			ResourceLocation registryName = CatnipServices.REGISTRIES.getKeyOrThrow(item.get()
					.asItem());
			return asResource(registryName.getPath() + suffix);
		};
	}

	/**
	 * Create a new processing recipe, with recipe definitions provided by the
	 * function, under the default namespace
	 */
	protected <T extends ProcessingRecipe<?>> GeneratedRecipe create(String name, UnaryOperator<ProcessingRecipeBuilder<T>> transform) {
		return create(asResource(name), transform);
	}

	/**
	 * Create a processing recipe with a single itemstack ingredient, using its id
	 * as the name of the recipe, under the default namespace
	 */
	protected <T extends ProcessingRecipe<?>> GeneratedRecipe create(Supplier<ItemLike> singleIngredient,
																								UnaryOperator<ProcessingRecipeBuilder<T>> transform) {
		return create(modid, singleIngredient, transform);
	}


	/**
	 * Gets a display name for this recipe generator.
	 * It is recommended to override this for a prettier name, however that is not
	 * required.
	 */
	@NotNull
	@Override
	public String getName() {
		return modid + "'s processing recipes: " + getRecipeType().getId()
			.getPath();
	}

}
