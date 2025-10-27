package com.simibubi.create.content.processing.sequenced;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import net.neoforged.neoforge.common.crafting.CompoundIngredient;

public class SequencedRecipe<T extends ProcessingRecipe<?, ?>> {
	public static final Codec<SequencedRecipe<?>> CODEC = Recipe.CODEC
		.comapFlatMap(recipe -> recipe instanceof ProcessingRecipe<?,?> processing && recipe instanceof IAssemblyRecipe
				? DataResult.success(new SequencedRecipe<>(processing))
				: DataResult.error(() -> recipe.getClass().getSimpleName() + " is not supported in Sequenced Assembly"),
			SequencedRecipe::getRecipe
		);
	public static final StreamCodec<RegistryFriendlyByteBuf, SequencedRecipe<?>> STREAM_CODEC = Recipe.STREAM_CODEC
		.map(recipe -> {
				if (recipe instanceof ProcessingRecipe<?,?> processing && recipe instanceof IAssemblyRecipe)
					return new SequencedRecipe<>(processing);
				throw new DecoderException("Unexpected " + recipe.getClass().getSimpleName() + " not supported in Sequenced Assembly");
			},
			SequencedRecipe::getRecipe
		);

	private final T wrapped;

	public SequencedRecipe(T wrapped) {
		this.wrapped = wrapped;
	}

	public IAssemblyRecipe getAsAssemblyRecipe() {
		return (IAssemblyRecipe) wrapped;
	}

	public T getRecipe() {
		return wrapped;
	}

	void initFromSequencedAssembly(SequencedAssemblyRecipe parent, boolean isFirst) {
		if (getAsAssemblyRecipe().supportsAssembly()) {
			Ingredient transit = Ingredient.of(parent.getTransitionalItem());
			wrapped.getIngredients()
					.set(0, isFirst ? CompoundIngredient.of(transit, parent.getIngredient()) : transit);
		}
	}
}
