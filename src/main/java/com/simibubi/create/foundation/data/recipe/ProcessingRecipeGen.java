package com.simibubi.create.foundation.data.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.simibubi.create.Create;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import com.simibubi.create.foundation.utility.RegisteredObjects;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fluids.FluidType;

public abstract class ProcessingRecipeGen extends CreateRecipeProvider {

	protected static final List<ProcessingRecipeGen> GENERATORS = new ArrayList<>();
	protected static final int BUCKET = FluidType.BUCKET_VOLUME;
	protected static final int BOTTLE = 250;

	public static void registerAll(DataGenerator gen, PackOutput output) {
		GENERATORS.add(new CrushingRecipeGen(output));
		GENERATORS.add(new MillingRecipeGen(output));
		GENERATORS.add(new CuttingRecipeGen(output));
		GENERATORS.add(new WashingRecipeGen(output));
		GENERATORS.add(new PolishingRecipeGen(output));
		GENERATORS.add(new DeployingRecipeGen(output));
		GENERATORS.add(new MixingRecipeGen(output));
		GENERATORS.add(new CompactingRecipeGen(output));
		GENERATORS.add(new PressingRecipeGen(output));
		GENERATORS.add(new FillingRecipeGen(output));
		GENERATORS.add(new EmptyingRecipeGen(output));
		GENERATORS.add(new HauntingRecipeGen(output));
		GENERATORS.add(new ItemApplicationRecipeGen(output));

		gen.addProvider(true, new DataProvider() {

			@Override
			public String getName() {
				return "Create's Processing Recipes";
			}

			@Override
			public CompletableFuture<?> run(CachedOutput dc) {
				return CompletableFuture.allOf(GENERATORS.stream()
					.map(gen -> gen.run(dc))
					.toArray(CompletableFuture[]::new));
			}
		});
	}

	public ProcessingRecipeGen(PackOutput generator) {
		super(generator);
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
					new ResourceLocation(namespace, RegisteredObjects.getKeyOrThrow(itemLike.asItem())
						.getPath())).withItemIngredients(Ingredient.of(itemLike)))
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

	protected Supplier<ResourceLocation> idWithSuffix(Supplier<ItemLike> item, String suffix) {
		return () -> {
			ResourceLocation registryName = RegisteredObjects.getKeyOrThrow(item.get()
				.asItem());
			return Create.asResource(registryName.getPath() + suffix);
		};
	}

	@Override
	public String getName() {
		return "Create's Processing Recipes: " + getRecipeType().getId()
			.getPath();
	}

}
