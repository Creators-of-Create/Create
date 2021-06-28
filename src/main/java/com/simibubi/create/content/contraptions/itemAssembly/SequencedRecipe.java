package com.simibubi.create.content.contraptions.itemAssembly;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeSerializer;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class SequencedRecipe<T extends ProcessingRecipe<?>> {

	T wrapped;

	public SequencedRecipe(T wrapped) {
		this.wrapped = wrapped;
	}

	public JsonObject toJson() {
		@SuppressWarnings("unchecked")
		ProcessingRecipeSerializer<T> serializer = (ProcessingRecipeSerializer<T>) wrapped.getSerializer();
		JsonObject json = new JsonObject();
		json.addProperty("type", ForgeRegistries.RECIPE_SERIALIZERS.getKey(serializer)
			.toString());
		serializer.write(json, wrapped);
		return json;
	}

	public static SequencedRecipe<?> fromJson(JsonObject json, SequencedAssemblyRecipe parent, int index) {
		ResourceLocation parentId = parent.getId();
		IRecipe<?> recipe = RecipeManager.deserializeRecipe(
			new ResourceLocation(parentId.getNamespace(), parentId.getPath() + "_step_" + index), json);
		if (recipe instanceof ProcessingRecipe<?>) {
			ProcessingRecipe<?> processingRecipe = (ProcessingRecipe<?>) recipe;
			if (processingRecipe.supportsAssembly()) {
				Ingredient transit = Ingredient.fromStacks(parent.transitionalItem.getStack());
				processingRecipe.getIngredients()
				.set(0, index == 0 ? Ingredient.merge(ImmutableList.of(transit, parent.ingredient)) : transit);
				SequencedRecipe<?> sequencedRecipe = new SequencedRecipe<>(processingRecipe);
				return sequencedRecipe;
			}
		}
		throw new JsonParseException("Not a supported recipe type");
	}

	public void writeToBuffer(PacketBuffer buffer) {
		@SuppressWarnings("unchecked")
		ProcessingRecipeSerializer<T> serializer = (ProcessingRecipeSerializer<T>) wrapped.getSerializer();
		buffer.writeResourceLocation(ForgeRegistries.RECIPE_SERIALIZERS.getKey(serializer));
		buffer.writeResourceLocation(wrapped.getId());
		serializer.write(buffer, wrapped);
	}

	public static SequencedRecipe<?> readFromBuffer(PacketBuffer buffer) {
		ResourceLocation resourcelocation = buffer.readResourceLocation();
		ResourceLocation resourcelocation1 = buffer.readResourceLocation();
		IRecipeSerializer<?> serializer = ForgeRegistries.RECIPE_SERIALIZERS.getValue(resourcelocation);
		if (!(serializer instanceof ProcessingRecipeSerializer))
			throw new JsonParseException("Not a supported recipe type");
		@SuppressWarnings("rawtypes")
		ProcessingRecipe recipe = (ProcessingRecipe) serializer.read(resourcelocation1, buffer);
		return new SequencedRecipe<>(recipe);
	}

}
