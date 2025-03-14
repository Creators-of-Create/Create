package com.simibubi.create.foundation.data.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.simibubi.create.Create;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import net.neoforged.neoforge.fluids.FluidType;

public abstract class ProcessingRecipeGen<B extends ProcessingRecipeBuilder<?, ?, B>> extends CreateRecipeProvider {

	protected static final List<ProcessingRecipeGen<?>> GENERATORS = new ArrayList<>();
	protected static final int BUCKET = FluidType.BUCKET_VOLUME;
	protected static final int BOTTLE = 250;

	public static void registerAll(DataGenerator gen, PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		GENERATORS.add(new CrushingRecipeGen(output, registries));
		GENERATORS.add(new MillingRecipeGen(output, registries));
		GENERATORS.add(new CuttingRecipeGen(output, registries));
		GENERATORS.add(new WashingRecipeGen(output, registries));
		GENERATORS.add(new PolishingRecipeGen(output, registries));
		GENERATORS.add(new DeployingRecipeGen(output, registries));
		GENERATORS.add(new MixingRecipeGen(output, registries));
		GENERATORS.add(new CompactingRecipeGen(output, registries));
		GENERATORS.add(new PressingRecipeGen(output, registries));
		GENERATORS.add(new FillingRecipeGen(output, registries));
		GENERATORS.add(new EmptyingRecipeGen(output, registries));
		GENERATORS.add(new HauntingRecipeGen(output, registries));
		GENERATORS.add(new ItemApplicationRecipeGen(output, registries));

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

	public ProcessingRecipeGen(PackOutput generator, CompletableFuture<HolderLookup.Provider> registries) {
		super(generator, registries);
	}

	/**
	 * Create a processing recipe with a single itemstack ingredient, using its id
	 * as the name of the recipe
	 */
	protected GeneratedRecipe create(String namespace, Supplier<ItemLike> singleIngredient, UnaryOperator<B> transform) {
		GeneratedRecipe generatedRecipe = c -> {
			ItemLike itemLike = singleIngredient.get();
			transform
				.apply(getBuilder(ResourceLocation.fromNamespaceAndPath(namespace, RegisteredObjectsHelper.getKeyOrThrow(itemLike.asItem()).getPath())).withItemIngredients(Ingredient.of(itemLike)))
				.build(c);
		};
		all.add(generatedRecipe);
		return generatedRecipe;
	}

	/**
	 * Create a processing recipe with a single itemstack ingredient, using its id
	 * as the name of the recipe
	 */
	GeneratedRecipe create(Supplier<ItemLike> singleIngredient, UnaryOperator<B> transform) {
		return create(Create.ID, singleIngredient, transform);
	}

	protected GeneratedRecipe createWithDeferredId(Supplier<ResourceLocation> name, UnaryOperator<B> transform) {
		GeneratedRecipe generatedRecipe =
			c -> transform.apply(getBuilder(name.get()))
				.build(c);
		all.add(generatedRecipe);
		return generatedRecipe;
	}

	/**
	 * Create a new processing recipe, with recipe definitions provided by the
	 * function
	 */
	protected GeneratedRecipe create(ResourceLocation name,
		UnaryOperator<B> transform) {
		return createWithDeferredId(() -> name, transform);
	}

	/**
	 * Create a new processing recipe, with recipe definitions provided by the
	 * function
	 */
	GeneratedRecipe create(String name,
		UnaryOperator<B> transform) {
		return create(Create.asResource(name), transform);
	}

	protected abstract IRecipeTypeInfo getRecipeType();

	protected abstract B getBuilder(ResourceLocation id);

	protected Supplier<ResourceLocation> idWithSuffix(Supplier<ItemLike> item, String suffix) {
		return () -> {
			ResourceLocation registryName = RegisteredObjectsHelper.getKeyOrThrow(item.get()
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
