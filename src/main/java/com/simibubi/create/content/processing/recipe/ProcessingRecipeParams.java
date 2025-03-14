package com.simibubi.create.content.processing.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredient;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;

import net.neoforged.neoforge.fluids.FluidStack;

public class ProcessingRecipeParams {
	protected static final List<String> KEYS =
		ImmutableList.of("ingredients", "results", "processing_time", "heat_requirement");
	protected static final MapCodec<List<Either<Ingredient, FluidIngredient>>> INGREDIENTS_CODEC =
		Codec.either(Ingredient.CODEC, FluidIngredient.CODEC).listOf().fieldOf("ingredients");
	protected static final MapCodec<List<Either<ProcessingOutput, FluidStack>>> RESULTS_CODEC =
		Codec.either(ProcessingOutput.CODEC, FluidStack.CODEC).listOf().fieldOf("results");
	protected static final	 MapCodec<Integer> PROCESSING_TIME_CODEC =
		Codec.INT.optionalFieldOf("processing_time", 0);
	protected static MapCodec<HeatCondition> HEAT_REQUIREMENT_CODEC =
		HeatCondition.CODEC.optionalFieldOf("heat_requirement", HeatCondition.NONE);
	public static MapCodec<ProcessingRecipeParams> CODEC = new MapCodec<>() {
		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return KEYS.stream().map(ops::createString);
		}

		@Override
		public <T> DataResult<ProcessingRecipeParams> decode(DynamicOps<T> ops, MapLike<T> input) {
			return new ProcessingRecipeParams().decode(ops, input).map(Function.identity());
		}

		@Override
		public <T> RecordBuilder<T> encode(ProcessingRecipeParams input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
			return input.encode(ops, prefix);
		}
	};
	public static StreamCodec<RegistryFriendlyByteBuf, ProcessingRecipeParams> STREAM_CODEC = StreamCodec.of(
		(buffer, params) -> params.encode(buffer),
		buffer -> Util.make(new ProcessingRecipeParams(), params -> params.decode(buffer))
	);

	protected NonNullList<Ingredient> ingredients;
	protected NonNullList<ProcessingOutput> results;
	protected NonNullList<FluidIngredient> fluidIngredients;
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

	protected <T> RecordBuilder<T> encode(DynamicOps<T> ops, RecordBuilder<T> builder) {
		List<Either<Ingredient, FluidIngredient>> ingredients =
			new ArrayList<>(this.ingredients.size() + this.fluidIngredients.size());
		this.ingredients.forEach(ingredient -> ingredients.add(Either.left(ingredient)));
		this.fluidIngredients.forEach(ingredient -> ingredients.add(Either.right(ingredient)));
		INGREDIENTS_CODEC.encode(ingredients, ops, builder);
		List<Either<ProcessingOutput, FluidStack>> results =
			new ArrayList<>(this.results.size() + this.fluidResults.size());
		this.results.forEach(result -> results.add(Either.left(result)));
		this.fluidResults.forEach(result -> results.add(Either.right(result)));
		RESULTS_CODEC.encode(results, ops, builder);
		PROCESSING_TIME_CODEC.encode(processingDuration, ops, builder);
		HEAT_REQUIREMENT_CODEC.encode(requiredHeat, ops, builder);
		return builder;
	}

	protected <T> DataResult<? extends ProcessingRecipeParams> decode(DynamicOps<T> ops, MapLike<T> input) {
		var ingredients = INGREDIENTS_CODEC.decode(ops, input);
		if (ingredients.isError()) return ingredients.map(it -> this);
		ingredients.getOrThrow().forEach(either -> either
			.ifLeft(this.ingredients::add)
			.ifRight(this.fluidIngredients::add));

		var results = RESULTS_CODEC.decode(ops, input);
		if (results.isError()) return results.map(it -> this);
		results.getOrThrow().forEach(either -> either
			.ifLeft(this.results::add)
			.ifRight(this.fluidResults::add));

		var processingTime = PROCESSING_TIME_CODEC.decode(ops, input);
		if (processingTime.isError()) return processingTime.map(it -> this);
		this.processingDuration = processingTime.getOrThrow();

		var heatRequirement = HEAT_REQUIREMENT_CODEC.decode(ops, input);
		if (heatRequirement.isError()) return heatRequirement.map(it -> this);
		this.requiredHeat = heatRequirement.getOrThrow();

		return DataResult.success(this);
	}

	protected void encode(RegistryFriendlyByteBuf buffer) {
		CatnipStreamCodecBuilders.nonNullList(Ingredient.CONTENTS_STREAM_CODEC).encode(buffer, ingredients);
		CatnipStreamCodecBuilders.nonNullList(FluidIngredient.STREAM_CODEC).encode(buffer, fluidIngredients);
		CatnipStreamCodecBuilders.nonNullList(ProcessingOutput.STREAM_CODEC).encode(buffer, results);
		CatnipStreamCodecBuilders.nonNullList(FluidStack.STREAM_CODEC).encode(buffer, fluidResults);
		ByteBufCodecs.VAR_INT.encode(buffer, processingDuration);
		HeatCondition.STREAM_CODEC.encode(buffer, requiredHeat);
	}

	protected void decode(RegistryFriendlyByteBuf buffer) {
		ingredients = CatnipStreamCodecBuilders.nonNullList(Ingredient.CONTENTS_STREAM_CODEC).decode(buffer);
		fluidIngredients = CatnipStreamCodecBuilders.nonNullList(FluidIngredient.STREAM_CODEC).decode(buffer);
		results = CatnipStreamCodecBuilders.nonNullList(ProcessingOutput.STREAM_CODEC).decode(buffer);
		fluidResults = CatnipStreamCodecBuilders.nonNullList(FluidStack.STREAM_CODEC).decode(buffer);
		processingDuration = ByteBufCodecs.VAR_INT.decode(buffer);
		requiredHeat = HeatCondition.STREAM_CODEC.decode(buffer);
	}
}
