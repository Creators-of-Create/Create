package com.simibubi.create.content.processing.recipe;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeFactory;
import com.simibubi.create.foundation.fluid.FluidIngredient;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import net.neoforged.neoforge.fluids.FluidStack;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ProcessingRecipeSerializer<T extends ProcessingRecipe<?>> implements RecipeSerializer<T> {

	private final ProcessingRecipeFactory<T> factory;

	public final MapCodec<T> CODEC = AllRecipeTypes.CODEC.dispatchMap(ProcessingRecipe::getRecipeType, AllRecipeTypes::processingCodec);

	public final StreamCodec<RegistryFriendlyByteBuf, T> STREAM_CODEC = StreamCodec.of(this::toNetwork, this::fromNetwork);

	public ProcessingRecipeSerializer(ProcessingRecipeFactory<T> factory) {
		this.factory = factory;
	}

	public static <T extends ProcessingRecipe<?>> MapCodec<T> codec(AllRecipeTypes recipeTypes) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.either(Ingredient.CODEC, FluidIngredient.CODEC).listOf().fieldOf("ingredients").forGetter(i -> {
					List<Either<Ingredient, FluidIngredient>> list = new ArrayList<>();
					i.getIngredients().forEach(o -> list.add(Either.left(o)));
					i.getFluidIngredients().forEach(o -> list.add(Either.right(o)));
					return list;
				}),
			Codec.either(FluidStack.CODEC, ProcessingOutput.CODEC).listOf().fieldOf("results").forGetter(i -> {
					List<Either<FluidStack, ProcessingOutput>> list = new ArrayList<>();
					i.getFluidResults().forEach(o -> list.add(Either.left(o)));
					i.getRollableResults().forEach(o -> list.add(Either.right(o)));
					return list;
				}), // Fluid and item outputs both using "id" as key, try deserializing as fluid first
			ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("processing_time", 0).forGetter(T::getProcessingDuration),
			HeatCondition.CODEC.optionalFieldOf("heat_requirement", HeatCondition.NONE).forGetter(T::getRequiredHeat)
		).apply(instance, (ingredients, results, processingTime, heatRequirement) -> {
			if (!(recipeTypes.serializerSupplier.get() instanceof ProcessingRecipeSerializer processingRecipeSerializer))
				throw new RuntimeException("Not a processing recipe serializer " + recipeTypes.serializerSupplier.get());

			ProcessingRecipeBuilder<T> builder = new ProcessingRecipeBuilder<T>(processingRecipeSerializer.getFactory(), recipeTypes.id);

			NonNullList<Ingredient> ingredientList = NonNullList.create();
			NonNullList<FluidIngredient> fluidIngredientList = NonNullList.create();

			NonNullList<ProcessingOutput> processingOutputList = NonNullList.create();
			NonNullList<FluidStack> fluidStackOutputList = NonNullList.create();

			for (Either<Ingredient, FluidIngredient> either : ingredients) {
				either.left().ifPresent(ingredientList::add);
				either.right().ifPresent(fluidIngredientList::add);
			}

			for (Either<FluidStack, ProcessingOutput> either : results) {
				either.left().ifPresent(fluidStackOutputList::add);
				either.right().ifPresent(processingOutputList::add);
			}

			builder.withItemIngredients(ingredientList)
					.withItemOutputs(processingOutputList)
					.withFluidIngredients(fluidIngredientList)
					.withFluidOutputs(fluidStackOutputList)
					.duration(processingTime)
					.requiresHeat(heatRequirement);

			return builder.build();
		}));
	}

	protected void toNetwork(RegistryFriendlyByteBuf buffer, T recipe) {
		ResourceLocation.STREAM_CODEC.encode(buffer, recipe.id);

		CatnipStreamCodecBuilders.nonNullList(Ingredient.CONTENTS_STREAM_CODEC).encode(buffer, recipe.ingredients);
		CatnipStreamCodecBuilders.nonNullList(FluidIngredient.STREAM_CODEC).encode(buffer, recipe.fluidIngredients);
		CatnipStreamCodecBuilders.nonNullList(ProcessingOutput.STREAM_CODEC).encode(buffer, recipe.results);
		CatnipStreamCodecBuilders.nonNullList(FluidStack.STREAM_CODEC).encode(buffer, recipe.fluidResults);

		ByteBufCodecs.VAR_INT.encode(buffer, recipe.getProcessingDuration());
		HeatCondition.STREAM_CODEC.encode(buffer, recipe.getRequiredHeat());

		recipe.writeAdditional(buffer);
	}

	protected T fromNetwork(RegistryFriendlyByteBuf buffer) {
		ResourceLocation recipeId = ResourceLocation.STREAM_CODEC.decode(buffer);

		NonNullList<Ingredient> ingredients = CatnipStreamCodecBuilders.nonNullList(Ingredient.CONTENTS_STREAM_CODEC).decode(buffer);
		NonNullList<FluidIngredient> fluidIngredients = CatnipStreamCodecBuilders.nonNullList(FluidIngredient.STREAM_CODEC).decode(buffer);
		NonNullList<ProcessingOutput> results = CatnipStreamCodecBuilders.nonNullList(ProcessingOutput.STREAM_CODEC).decode(buffer);
		NonNullList<FluidStack> fluidResults = CatnipStreamCodecBuilders.nonNullList(FluidStack.STREAM_CODEC).decode(buffer);

		T recipe = new ProcessingRecipeBuilder<>(factory, recipeId).withItemIngredients(ingredients)
			.withItemOutputs(results)
			.withFluidIngredients(fluidIngredients)
			.withFluidOutputs(fluidResults)
			.duration(ByteBufCodecs.VAR_INT.decode(buffer))
			.requiresHeat(HeatCondition.STREAM_CODEC.decode(buffer))
			.build();
		recipe.readAdditional(buffer);
		return recipe;
	}

	@Override
	public MapCodec<T> codec() {
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
		return STREAM_CODEC;
	}

	public ProcessingRecipeFactory<T> getFactory() {
		return factory;
	}
}
