package com.simibubi.create.content.contraptions.itemAssembly;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class SequencedAssemblyRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>>
	implements IRecipeSerializer<SequencedAssemblyRecipe> {

	public SequencedAssemblyRecipeSerializer() {}

	protected void writeToJson(JsonObject json, SequencedAssemblyRecipe recipe) {
		JsonArray nestedRecipes = new JsonArray();
		JsonArray results = new JsonArray();
		json.add("ingredient", recipe.getIngredient().serialize());
		recipe.getSequence().forEach(i -> nestedRecipes.add(i.toJson()));
		recipe.resultPool.forEach(p -> results.add(p.serialize()));
		json.add("transitionalItem", recipe.transitionalItem.serialize());
		json.add("sequence", nestedRecipes);
		json.add("results", results);
		json.addProperty("loops", recipe.loops);
	}

	protected SequencedAssemblyRecipe readFromJson(ResourceLocation recipeId, JsonObject json) {
		SequencedAssemblyRecipe recipe = new SequencedAssemblyRecipe(recipeId, this);
		recipe.ingredient = Ingredient.deserialize(json.get("ingredient"));
		recipe.transitionalItem = ProcessingOutput.deserialize(JSONUtils.getJsonObject(json, "transitionalItem"));
		int i = 0;
		for (JsonElement je : JSONUtils.getJsonArray(json, "sequence"))
			recipe.getSequence().add(SequencedRecipe.fromJson(je.getAsJsonObject(), recipe, i++));
		for (JsonElement je : JSONUtils.getJsonArray(json, "results"))
			recipe.resultPool.add(ProcessingOutput.deserialize(je));
		if (JSONUtils.hasField(json, "loops")) 
			recipe.loops = JSONUtils.getInt(json, "loops");
		return recipe;
	}

	protected void writeToBuffer(PacketBuffer buffer, SequencedAssemblyRecipe recipe) {
		recipe.getIngredient().write(buffer);
		buffer.writeVarInt(recipe.getSequence().size());
		recipe.getSequence().forEach(sr -> sr.writeToBuffer(buffer));
		buffer.writeVarInt(recipe.resultPool.size());
		recipe.resultPool.forEach(sr -> sr.write(buffer));
		recipe.transitionalItem.write(buffer);
		buffer.writeInt(recipe.loops);
	}

	protected SequencedAssemblyRecipe readFromBuffer(ResourceLocation recipeId, PacketBuffer buffer) {
		SequencedAssemblyRecipe recipe = new SequencedAssemblyRecipe(recipeId, this);
		recipe.ingredient = Ingredient.read(buffer);
		int size = buffer.readVarInt();
		for (int i = 0; i < size; i++)
			recipe.getSequence().add(SequencedRecipe.readFromBuffer(buffer));
		size = buffer.readVarInt();
		for (int i = 0; i < size; i++)
			recipe.resultPool.add(ProcessingOutput.read(buffer));
		recipe.transitionalItem = ProcessingOutput.read(buffer);
		recipe.loops = buffer.readInt();
		return recipe;
	}

	public final void write(JsonObject json, SequencedAssemblyRecipe recipe) {
		writeToJson(json, recipe);
	}

	@Override
	public final SequencedAssemblyRecipe read(ResourceLocation id, JsonObject json) {
		return readFromJson(id, json);
	}

	@Override
	public final void write(PacketBuffer buffer, SequencedAssemblyRecipe recipe) {
		writeToBuffer(buffer, recipe);
	}

	@Override
	public final SequencedAssemblyRecipe read(ResourceLocation id, PacketBuffer buffer) {
		return readFromBuffer(id, buffer);
	}

}
