package com.simibubi.create.content.contraptions.itemAssembly;

import java.util.Arrays;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeSerializer;
import io.github.fabricators_of_create.porting_lib.mixin.common.accessor.IngredientAccessor;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SequencedRecipe<T extends ProcessingRecipe<?>> {

	private T wrapped;

	public SequencedRecipe(T wrapped) {
		this.wrapped = wrapped;
	}

	public IAssemblyRecipe getAsAssemblyRecipe() {
		return (IAssemblyRecipe) wrapped;
	}

	public ProcessingRecipe<?> getRecipe() {
		return wrapped;
	}

	public JsonObject toJson() {
		@SuppressWarnings("unchecked")
		ProcessingRecipeSerializer<T> serializer = (ProcessingRecipeSerializer<T>) wrapped.getSerializer();
		JsonObject json = new JsonObject();
		json.addProperty("type", Registry.RECIPE_SERIALIZER.getKey(serializer)
			.toString());
		serializer.write(json, wrapped);
		return json;
	}

	public static SequencedRecipe<?> fromJson(JsonObject json, SequencedAssemblyRecipe parent, int index) {
		ResourceLocation parentId = parent.getId();
		Recipe<?> recipe = RecipeManager.fromJson(
			new ResourceLocation(parentId.getNamespace(), parentId.getPath() + "_step_" + index), json);
		if (recipe instanceof ProcessingRecipe<?> && recipe instanceof IAssemblyRecipe) {
			ProcessingRecipe<?> processingRecipe = (ProcessingRecipe<?>) recipe;
			IAssemblyRecipe assemblyRecipe = (IAssemblyRecipe) recipe;
			if (assemblyRecipe.supportsAssembly()) {
				Ingredient transit = Ingredient.of(parent.getTransitionalItem());

				processingRecipe.getIngredients()
					.set(0, index == 0 ? IngredientAccessor.create$fromValues(ImmutableList.of(transit, parent.getIngredient()).stream().flatMap(i -> Arrays.stream(((IngredientAccessor) (Object) i).create$getAcceptedItems()))) : transit);
				SequencedRecipe<?> sequencedRecipe = new SequencedRecipe<>(processingRecipe);
				return sequencedRecipe;
			}
		}
		throw new JsonParseException("Not a supported recipe type");
	}

	public void writeToBuffer(FriendlyByteBuf buffer) {
		@SuppressWarnings("unchecked")
		ProcessingRecipeSerializer<T> serializer = (ProcessingRecipeSerializer<T>) wrapped.getSerializer();
		buffer.writeResourceLocation(Registry.RECIPE_SERIALIZER.getKey(serializer));
		buffer.writeResourceLocation(wrapped.getId());
		serializer.toNetwork(buffer, wrapped);
	}

	public static SequencedRecipe<?> readFromBuffer(FriendlyByteBuf buffer) {
		ResourceLocation resourcelocation = buffer.readResourceLocation();
		ResourceLocation resourcelocation1 = buffer.readResourceLocation();
		RecipeSerializer<?> serializer = Registry.RECIPE_SERIALIZER.get(resourcelocation);
		if (!(serializer instanceof ProcessingRecipeSerializer))
			throw new JsonParseException("Not a supported recipe type");
		@SuppressWarnings("rawtypes")
		ProcessingRecipe recipe = (ProcessingRecipe) serializer.fromNetwork(resourcelocation1, buffer);
		return new SequencedRecipe<>(recipe);
	}

}
