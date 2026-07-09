package com.simibubi.create.content.processing.sequenced;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;

import net.createmod.catnip.api.data.codec.stream.CatnipStreamCodecBuilders;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SequencedAssemblyRecipeSerializer {
	private static final MapCodec<SequencedAssemblyRecipe> CODEC = RecordCodecBuilder.mapCodec(
		i -> i.group(
			Ingredient.CODEC.fieldOf("ingredient").forGetter(SequencedAssemblyRecipe::getIngredient),
			ProcessingOutput.CODEC.fieldOf("transitional_item").forGetter(r -> r.transitionalItem),
			SequencedRecipe.CODEC.listOf().fieldOf("sequence").forGetter(SequencedAssemblyRecipe::getSequence),
			ProcessingOutput.CODEC.listOf().fieldOf("results").forGetter(r -> r.resultPool),
			ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("loops", 1).forGetter(SequencedAssemblyRecipe::getLoops)
		).apply(i, (ingredient, transitionalItem, sequence, results, loops) -> {
			SequencedAssemblyRecipe recipe = new SequencedAssemblyRecipe(create());
			recipe.ingredient = ingredient;
			recipe.transitionalItem = transitionalItem;
			recipe.sequence.addAll(sequence);
			recipe.resultPool.addAll(results);
			recipe.loops = loops;

			for (int j = 0; j < recipe.sequence.size(); j++)
				sequence.get(j).initFromSequencedAssembly(recipe, j == 0);

			return recipe;
		})
	);

	private static final StreamCodec<RegistryFriendlyByteBuf, SequencedAssemblyRecipe> STREAM_CODEC = StreamCodec.composite(
		Ingredient.CONTENTS_STREAM_CODEC, r -> r.ingredient,
		CatnipStreamCodecBuilders.list(SequencedRecipe.STREAM_CODEC), SequencedAssemblyRecipe::getSequence,
		CatnipStreamCodecBuilders.list(ProcessingOutput.STREAM_CODEC), r -> r.resultPool,
		ProcessingOutput.STREAM_CODEC, r -> r.transitionalItem,
		ByteBufCodecs.VAR_INT, r -> r.loops,
		(ingredient, sequence, results, transitionalItem, loops) -> {
			SequencedAssemblyRecipe recipe = new SequencedAssemblyRecipe(create());
			recipe.ingredient = ingredient;
			recipe.getSequence().addAll(sequence);
			recipe.resultPool.addAll(results);
			recipe.transitionalItem = transitionalItem;
			recipe.loops = loops;
			return recipe;
		}
	);

	private static final RecipeSerializer<SequencedAssemblyRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	public static RecipeSerializer<SequencedAssemblyRecipe> create() {
		return SERIALIZER;
	}
}
