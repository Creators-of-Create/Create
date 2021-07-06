package com.simibubi.create.foundation.data.recipe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeSerializer;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidAttributes;

public abstract class ProcessingRecipeGen extends CreateRecipeProvider {

	protected static List<ProcessingRecipeGen> generators = new ArrayList<>();
	protected static final int BUCKET = FluidAttributes.BUCKET_VOLUME;
	protected static final int BOTTLE = 250;

	public static void registerAll(DataGenerator gen) {
		generators.add(new CrushingRecipeGen(gen));
		generators.add(new MillingRecipeGen(gen));
		generators.add(new CuttingRecipeGen(gen));
		generators.add(new WashingRecipeGen(gen));
		generators.add(new PolishingRecipeGen(gen));
		generators.add(new DeployingRecipeGen(gen));
		generators.add(new MixingRecipeGen(gen));
		generators.add(new CompactingRecipeGen(gen));
		generators.add(new PressingRecipeGen(gen));
		generators.add(new FillingRecipeGen(gen));
		generators.add(new EmptyingRecipeGen(gen));

		gen.addProvider(new IDataProvider() {

			@Override
			public String getName() {
				return "Create's Processing Recipes";
			}

			@Override
			public void act(DirectoryCache dc) throws IOException {
				generators.forEach(g -> {
					try {
						g.act(dc);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			}
		});
	}

	public ProcessingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	/**
	 * Create a processing recipe with a single itemstack ingredient, using its id
	 * as the name of the recipe
	 */
	protected <T extends ProcessingRecipe<?>> GeneratedRecipe create(String namespace, Supplier<IItemProvider> singleIngredient,
		UnaryOperator<ProcessingRecipeBuilder<T>> transform) {
		ProcessingRecipeSerializer<T> serializer = getSerializer();
		GeneratedRecipe generatedRecipe = c -> {
			IItemProvider iItemProvider = singleIngredient.get();
			transform
				.apply(new ProcessingRecipeBuilder<>(serializer.getFactory(), new ResourceLocation(namespace, iItemProvider.asItem()
					.getRegistryName()
					.getPath())).withItemIngredients(Ingredient.fromItems(iItemProvider)))
				.build(c);
		};
		all.add(generatedRecipe);
		return generatedRecipe;
	}

	/**
	 * Create a processing recipe with a single itemstack ingredient, using its id
	 * as the name of the recipe
	 */
	protected <T extends ProcessingRecipe<?>> GeneratedRecipe create(Supplier<IItemProvider> singleIngredient,
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
	protected <T extends ProcessingRecipe<?>> GeneratedRecipe create(String name,
																	 UnaryOperator<ProcessingRecipeBuilder<T>> transform) {
		return create(Create.asResource(name), transform);
	}

	@SuppressWarnings("unchecked")
	protected  <T extends ProcessingRecipe<?>> ProcessingRecipeSerializer<T> getSerializer() {
		ProcessingRecipeSerializer<T> serializer = (ProcessingRecipeSerializer<T>) getRecipeType().serializer;
		return serializer;
	}

	@Override
	public final String getName() {
		return "Create's Processing Recipes: " + getRecipeType();
	}

	protected abstract AllRecipeTypes getRecipeType();

}
