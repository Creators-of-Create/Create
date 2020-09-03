package com.simibubi.create.foundation.data.recipe;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeSerializer;

import net.minecraft.data.DataGenerator;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.fluids.FluidAttributes;

public abstract class ProcessingRecipeGen extends CreateRecipeProvider {

	protected static final int BUCKET = FluidAttributes.BUCKET_VOLUME;
	protected static final int BOTTLE = 250;

	public static void registerAll(DataGenerator gen) {
		gen.addProvider(new CrushingRecipeGen(gen));
		gen.addProvider(new MillingRecipeGen(gen));
		gen.addProvider(new CuttingRecipeGen(gen));
		gen.addProvider(new WashingRecipeGen(gen));
		gen.addProvider(new PolishingRecipeGen(gen));
		gen.addProvider(new MixingRecipeGen(gen));
		gen.addProvider(new PressingRecipeGen(gen));
	}
	
	public ProcessingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}
	
	/**
	 * Create a processing recipe with a single itemstack ingredient, using its id
	 * as the name of the recipe
	 */
	protected <T extends ProcessingRecipe<?>> GeneratedRecipe create(Supplier<IItemProvider> singleIngredient,
		UnaryOperator<ProcessingRecipeBuilder<T>> transform) {
		ProcessingRecipeSerializer<T> serializer = getSerializer();
		GeneratedRecipe generatedRecipe = c -> {
			IItemProvider iItemProvider = singleIngredient.get();
			transform
				.apply(new ProcessingRecipeBuilder<>(serializer.getFactory(), Create.asResource(iItemProvider.asItem()
					.getRegistryName()
					.getPath())).withItemIngredients(Ingredient.fromItems(iItemProvider)))
				.build(c);
		};
		all.add(generatedRecipe);
		return generatedRecipe;
	}

	/**
	 * Create a new processing recipe, with recipe definitions provided by the
	 * function
	 */
	protected <T extends ProcessingRecipe<?>> GeneratedRecipe create(String name,
		UnaryOperator<ProcessingRecipeBuilder<T>> transform) {
		ProcessingRecipeSerializer<T> serializer = getSerializer();
		GeneratedRecipe generatedRecipe =
			c -> transform.apply(new ProcessingRecipeBuilder<>(serializer.getFactory(), Create.asResource(name)))
				.build(c);
		all.add(generatedRecipe);
		return generatedRecipe;
	}

	@SuppressWarnings("unchecked")
	private <T extends ProcessingRecipe<?>> ProcessingRecipeSerializer<T> getSerializer() {
		ProcessingRecipeSerializer<T> serializer = (ProcessingRecipeSerializer<T>) getRecipeType().serializer;
		return serializer;
	}

	@Override
	public final String getName() {
		return "Create's Processing Recipes: " + getRecipeType();
	}
	
	protected abstract AllRecipeTypes getRecipeType();

}
