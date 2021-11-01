package com.simibubi.create.foundation.data.recipe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeSerializer;
import com.simibubi.create.foundation.utility.recipe.IRecipeTypeInfo;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.data.DataProvider;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidAttributes;

public abstract class ProcessingRecipeGen extends CreateRecipeProvider {

	protected static final List<ProcessingRecipeGen> GENERATORS = new ArrayList<>();
	protected static final int BUCKET = FluidAttributes.BUCKET_VOLUME;
	protected static final int BOTTLE = 250;

	public static void registerAll(DataGenerator gen) {
		GENERATORS.add(new CrushingRecipeGen(gen));
		GENERATORS.add(new MillingRecipeGen(gen));
		GENERATORS.add(new CuttingRecipeGen(gen));
		GENERATORS.add(new WashingRecipeGen(gen));
		GENERATORS.add(new PolishingRecipeGen(gen));
		GENERATORS.add(new DeployingRecipeGen(gen));
		GENERATORS.add(new MixingRecipeGen(gen));
		GENERATORS.add(new CompactingRecipeGen(gen));
		GENERATORS.add(new PressingRecipeGen(gen));
		GENERATORS.add(new FillingRecipeGen(gen));
		GENERATORS.add(new EmptyingRecipeGen(gen));

		gen.addProvider(new DataProvider() {

			@Override
			public String getName() {
				return "Create's Processing Recipes";
			}

			@Override
			public void run(HashCache dc) throws IOException {
				GENERATORS.forEach(g -> {
					try {
						g.run(dc);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			}
		});
	}

	public ProcessingRecipeGen(DataGenerator generator) {
		super(generator);
	}

	/**
	 * Create a processing recipe with a single itemstack ingredient, using its id
	 * as the name of the recipe
	 */
	protected <T extends ProcessingRecipe<?>> GeneratedRecipe create(String namespace, Supplier<ItemLike> singleIngredient,
		UnaryOperator<ProcessingRecipeBuilder<T>> transform) {
		ProcessingRecipeSerializer<T> serializer = getSerializer();
		GeneratedRecipe generatedRecipe = c -> {
			ItemLike iItemProvider = singleIngredient.get();
			transform
				.apply(new ProcessingRecipeBuilder<>(serializer.getFactory(), new ResourceLocation(namespace, iItemProvider.asItem()
					.getRegistryName()
					.getPath())).withItemIngredients(Ingredient.of(iItemProvider)))
				.build(c);
		};
		all.add(generatedRecipe);
		return generatedRecipe;
	}

	/**
	 * Create a processing recipe with a single itemstack ingredient, using its id
	 * as the name of the recipe
	 */
	<T extends ProcessingRecipe<?>> GeneratedRecipe create(Supplier<ItemLike> singleIngredient,
																	 UnaryOperator<ProcessingRecipeBuilder<T>> transform) {
		return create(Create.ID, singleIngredient, transform);
	}

	/**
	 * Create a new processing recipe, with recipe definitions provided by the
	 * function
	 */
	protected <T extends ProcessingRecipe<?>> GeneratedRecipe create(ResourceLocation name,
																	 UnaryOperator<ProcessingRecipeBuilder<T>> transform) {
		ProcessingRecipeSerializer<T> serializer = getSerializer();
		GeneratedRecipe generatedRecipe =
			c -> transform.apply(new ProcessingRecipeBuilder<>(serializer.getFactory(), name))
				.build(c);
		all.add(generatedRecipe);
		return generatedRecipe;
	}

	/**
	 * Create a new processing recipe, with recipe definitions provided by the
	 * function
	 */
	<T extends ProcessingRecipe<?>> GeneratedRecipe create(String name,
																	 UnaryOperator<ProcessingRecipeBuilder<T>> transform) {
		return create(Create.asResource(name), transform);
	}

	protected abstract IRecipeTypeInfo getRecipeType();

	protected <T extends ProcessingRecipe<?>> ProcessingRecipeSerializer<T> getSerializer() {
		return getRecipeType().getSerializer();
	}

	@Override
	public String getName() {
		return "Create's Processing Recipes: " + getRecipeType().getId().getPath();
	}

}
