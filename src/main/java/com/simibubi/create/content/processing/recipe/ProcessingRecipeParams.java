package com.simibubi.create.content.processing.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.codec.CreateCodecs;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class ProcessingRecipeParams {
	public static MapCodec<ProcessingRecipeParams> CODEC = codec(ProcessingRecipeParams::new);
	public static StreamCodec<RegistryFriendlyByteBuf, ProcessingRecipeParams> STREAM_CODEC = streamCodec(ProcessingRecipeParams::new);

	protected NonNullList<Ingredient> ingredients;
	protected NonNullList<ProcessingOutput> results;
	protected NonNullList<SizedFluidIngredient> fluidIngredients;
	protected NonNullList<FluidStack> fluidResults;
	protected int processingDuration;
	protected HeatCondition requiredHeat;

	protected ProcessingRecipeParams() {
		ingredients = NonNullList.create();
		results = NonNullList.create();
		fluidIngredients = NonNullList.create();
		fluidResults = NonNullList.create();
		processingDuration = 0;
		requiredHeat = HeatCondition.NONE;
	}

	protected static <P extends ProcessingRecipeParams> MapCodec<P> codec(Supplier<P> factory) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.either(CreateCodecs.SIZED_FLUID_INGREDIENT, Ingredient.CODEC).listOf().fieldOf("ingredients")
				.forGetter(ProcessingRecipeParams::ingredients),
			Codec.either(FluidStack.CODEC, ProcessingOutput.CODEC).listOf().fieldOf("results")
				.forGetter(ProcessingRecipeParams::results),
			Codec.INT.optionalFieldOf("processing_time", 0)
				.forGetter(ProcessingRecipeParams::processingDuration),
			HeatCondition.CODEC.optionalFieldOf("heat_requirement", HeatCondition.NONE)
				.forGetter(ProcessingRecipeParams::requiredHeat)
		).apply(instance, (ingredients, results, processingDuration, requiredHeat) -> {
			P params = factory.get();
			ingredients.forEach(either -> either
				.ifRight(params.ingredients::add)
				.ifLeft(params.fluidIngredients::add));
			results.forEach(either -> either
				.ifRight(params.results::add)
				.ifLeft(params.fluidResults::add));
			params.processingDuration = processingDuration;
			params.requiredHeat = requiredHeat;
			return params;
		}));
	}

	protected static <P extends ProcessingRecipeParams> StreamCodec<RegistryFriendlyByteBuf, P> streamCodec(Supplier<P> factory) {
		return StreamCodec.of(
			(buffer, params) -> params.encode(buffer),
			buffer -> {
				P params = factory.get();
				params.decode(buffer);
				return params;
			});
	}

	protected final List<Either<SizedFluidIngredient, Ingredient>> ingredients() {
		List<Either<SizedFluidIngredient, Ingredient>> ingredients =
			new ArrayList<>(this.ingredients.size() + this.fluidIngredients.size());
		this.ingredients.forEach(ingredient -> ingredients.add(Either.right(ingredient)));
		this.fluidIngredients.forEach(ingredient -> ingredients.add(Either.left(ingredient)));
		return ingredients;
	}

	protected final List<Either<FluidStack, ProcessingOutput>> results() {
		List<Either<FluidStack, ProcessingOutput>> results =
			new ArrayList<>(this.results.size() + this.fluidResults.size());
		this.results.forEach(result -> results.add(Either.right(result)));
		this.fluidResults.forEach(result -> results.add(Either.left(result)));
		return results;
	}

	protected final int processingDuration() {
		return processingDuration;
	}

	protected final HeatCondition requiredHeat() {
		return requiredHeat;
	}

	protected void encode(RegistryFriendlyByteBuf buffer) {
		CatnipStreamCodecBuilders.nonNullList(Ingredient.CONTENTS_STREAM_CODEC).encode(buffer, ingredients);
		CatnipStreamCodecBuilders.nonNullList(SizedFluidIngredient.STREAM_CODEC).encode(buffer, fluidIngredients);
		CatnipStreamCodecBuilders.nonNullList(ProcessingOutput.STREAM_CODEC).encode(buffer, results);
		CatnipStreamCodecBuilders.nonNullList(FluidStack.STREAM_CODEC).encode(buffer, fluidResults);
		ByteBufCodecs.VAR_INT.encode(buffer, processingDuration);
		HeatCondition.STREAM_CODEC.encode(buffer, requiredHeat);
	}

	protected void decode(RegistryFriendlyByteBuf buffer) {
		ingredients = CatnipStreamCodecBuilders.nonNullList(Ingredient.CONTENTS_STREAM_CODEC).decode(buffer);
		fluidIngredients = CatnipStreamCodecBuilders.nonNullList(SizedFluidIngredient.STREAM_CODEC).decode(buffer);
		results = CatnipStreamCodecBuilders.nonNullList(ProcessingOutput.STREAM_CODEC).decode(buffer);
		fluidResults = CatnipStreamCodecBuilders.nonNullList(FluidStack.STREAM_CODEC).decode(buffer);
		processingDuration = ByteBufCodecs.VAR_INT.decode(buffer);
		requiredHeat = HeatCondition.STREAM_CODEC.decode(buffer);
	}
}
