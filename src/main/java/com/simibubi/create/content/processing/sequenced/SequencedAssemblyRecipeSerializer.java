package com.simibubi.create.content.processing.sequenced;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SequencedAssemblyRecipeSerializer implements RecipeSerializer<SequencedAssemblyRecipe> {

	public SequencedAssemblyRecipeSerializer() {}

	protected void writeToJson(JsonObject json, SequencedAssemblyRecipe recipe) {
		JsonArray nestedRecipes = new JsonArray();
		JsonArray results = new JsonArray();
		json.add("ingredient", recipe.getIngredient().toJson());
		recipe.getSequence().forEach(i -> nestedRecipes.add(i.toJson()));
		recipe.resultPool.forEach(p -> results.add(p.serialize()));
		json.add("transitionalItem", recipe.transitionalItem.serialize());
		json.add("sequence", nestedRecipes);
		json.add("results", results);
		json.addProperty("loops", recipe.loops);
	}

	protected SequencedAssemblyRecipe readFromJson(ResourceLocation recipeId, JsonObject json) {
		SequencedAssemblyRecipe recipe = new SequencedAssemblyRecipe(recipeId, this);
		recipe.ingredient = Ingredient.fromJson(json.get("ingredient"));
		recipe.transitionalItem = ProcessingOutput.deserialize(GsonHelper.getAsJsonObject(json, "transitionalItem"));
		int i = 0;
		for (JsonElement je : GsonHelper.getAsJsonArray(json, "sequence"))
			recipe.getSequence().add(SequencedRecipe.fromJson(je.getAsJsonObject(), recipe, i++));
		for (JsonElement je : GsonHelper.getAsJsonArray(json, "results"))
			recipe.resultPool.add(ProcessingOutput.deserialize(je));
		if (GsonHelper.isValidNode(json, "loops")) 
			recipe.loops = GsonHelper.getAsInt(json, "loops");
		return recipe;
	}

	protected void writeToBuffer(FriendlyByteBuf buffer, SequencedAssemblyRecipe recipe) {
		recipe.getIngredient().toNetwork(buffer);
		buffer.writeVarInt(recipe.getSequence().size());
		recipe.getSequence().forEach(sr -> sr.writeToBuffer(buffer));
		buffer.writeVarInt(recipe.resultPool.size());
		recipe.resultPool.forEach(sr -> sr.write(buffer));
		recipe.transitionalItem.write(buffer);
		buffer.writeInt(recipe.loops);
	}

	protected SequencedAssemblyRecipe readFromBuffer(ResourceLocation recipeId, FriendlyByteBuf buffer) {
		SequencedAssemblyRecipe recipe = new SequencedAssemblyRecipe(recipeId, this);
		recipe.ingredient = Ingredient.fromNetwork(buffer);
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
	public final SequencedAssemblyRecipe fromJson(ResourceLocation id, JsonObject json) {
		return readFromJson(id, json);
	}

	@Override
	public final void toNetwork(FriendlyByteBuf buffer, SequencedAssemblyRecipe recipe) {
		writeToBuffer(buffer, recipe);
	}

	@Override
	public final SequencedAssemblyRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
		return readFromBuffer(id, buffer);
	}

}
