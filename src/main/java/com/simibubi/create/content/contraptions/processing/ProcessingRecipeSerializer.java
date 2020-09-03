package com.simibubi.create.content.contraptions.processing;

import javax.annotation.ParametersAreNonnullByDefault;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder.ProcessingRecipeFactory;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.fluid.FluidIngredient;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistryEntry;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ProcessingRecipeSerializer<T extends ProcessingRecipe<?>> extends ForgeRegistryEntry<IRecipeSerializer<?>>
	implements IRecipeSerializer<T> {

	private final ProcessingRecipeFactory<T> factory;

	public ProcessingRecipeSerializer(ProcessingRecipeFactory<T> factory) {
		this.factory = factory;
	}

	protected void writeToJson(JsonObject json, T recipe) {
		JsonArray jsonIngredients = new JsonArray();
		JsonArray jsonOutputs = new JsonArray();

		recipe.getIngredients()
			.forEach(i -> jsonIngredients.add(i.serialize()));
		recipe.getFluidIngredients()
			.forEach(i -> jsonIngredients.add(i.serialize()));

		recipe.getRollableResults()
			.forEach(o -> jsonOutputs.add(o.serialize()));
		recipe.getFluidResults()
			.forEach(o -> jsonOutputs.add(FluidHelper.serializeFluidStack(o)));

		json.add("ingredients", jsonIngredients);
		json.add("results", jsonOutputs);

		int processingDuration = recipe.getProcessingDuration();
		if (processingDuration > 0)
			json.addProperty("processingTime", processingDuration);

		HeatCondition requiredHeat = recipe.getRequiredHeat();
		if (requiredHeat != HeatCondition.NONE)
			json.addProperty("heatRequirement", requiredHeat.serialize());

		recipe.writeAdditional(json);
	}

	protected T readFromJson(ResourceLocation recipeId, JsonObject json) {
		ProcessingRecipeBuilder<T> builder = new ProcessingRecipeBuilder<>(factory, recipeId);
		NonNullList<Ingredient> ingredients = NonNullList.create();
		NonNullList<FluidIngredient> fluidIngredients = NonNullList.create();
		NonNullList<ProcessingOutput> results = NonNullList.create();
		NonNullList<FluidStack> fluidResults = NonNullList.create();

		for (JsonElement je : JSONUtils.getJsonArray(json, "ingredients")) {
			if (FluidIngredient.isFluidIngredient(je))
				fluidIngredients.add(FluidIngredient.deserialize(je));
			else
				ingredients.add(Ingredient.deserialize(je));
		}

		for (JsonElement je : JSONUtils.getJsonArray(json, "results")) {
			JsonObject jsonObject = je.getAsJsonObject();
			if (JSONUtils.hasField(jsonObject, "fluid"))
				fluidResults.add(FluidHelper.deserializeFluidStack(jsonObject));
			else
				results.add(ProcessingOutput.deserialize(je));
		}

		builder.withItemIngredients(ingredients)
			.withItemOutputs(results)
			.withFluidIngredients(fluidIngredients)
			.withFluidOutputs(fluidResults);

		if (JSONUtils.hasField(json, "processingTime"))
			builder.duration(JSONUtils.getInt(json, "processingTime"));
		if (JSONUtils.hasField(json, "heatRequirement"))
			builder.requiresHeat(HeatCondition.deserialize(JSONUtils.getString(json, "heatRequirement")));

		return builder.build();
	}

	protected void writeToBuffer(PacketBuffer buffer, T recipe) {
		NonNullList<Ingredient> ingredients = recipe.getIngredients();
		NonNullList<FluidIngredient> fluidIngredients = recipe.getFluidIngredients();
		NonNullList<ProcessingOutput> outputs = recipe.getRollableResults();
		NonNullList<FluidStack> fluidOutputs = recipe.getFluidResults();

		buffer.writeVarInt(ingredients.size());
		ingredients.forEach(i -> i.write(buffer));
		buffer.writeVarInt(fluidIngredients.size());
		fluidIngredients.forEach(i -> i.write(buffer));

		buffer.writeVarInt(outputs.size());
		outputs.forEach(o -> o.write(buffer));
		buffer.writeVarInt(fluidOutputs.size());
		fluidOutputs.forEach(o -> o.writeToPacket(buffer));

		buffer.writeVarInt(recipe.getProcessingDuration());
		buffer.writeVarInt(recipe.getRequiredHeat()
			.ordinal());
	}

	protected T readFromBuffer(ResourceLocation recipeId, PacketBuffer buffer) {
		NonNullList<Ingredient> ingredients = NonNullList.create();
		NonNullList<FluidIngredient> fluidIngredients = NonNullList.create();
		NonNullList<ProcessingOutput> results = NonNullList.create();
		NonNullList<FluidStack> fluidResults = NonNullList.create();

		for (int i = 0; i < buffer.readVarInt(); i++)
			ingredients.add(Ingredient.read(buffer));
		for (int i = 0; i < buffer.readVarInt(); i++)
			fluidIngredients.add(FluidIngredient.read(buffer));
		for (int i = 0; i < buffer.readVarInt(); i++)
			results.add(ProcessingOutput.read(buffer));
		for (int i = 0; i < buffer.readVarInt(); i++)
			fluidResults.add(FluidStack.readFromPacket(buffer));

		return new ProcessingRecipeBuilder<>(factory, recipeId).withItemIngredients(ingredients)
			.withItemOutputs(results)
			.withFluidIngredients(fluidIngredients)
			.withFluidOutputs(fluidResults)
			.duration(buffer.readVarInt())
			.requiresHeat(HeatCondition.values()[buffer.readVarInt()])
			.build();
	}

	public final void write(JsonObject json, T recipe) {
		writeToJson(json, recipe);
	}

	@Override
	public final T read(ResourceLocation id, JsonObject json) {
		return readFromJson(id, json);
	}

	@Override
	public final void write(PacketBuffer buffer, T recipe) {
		writeToBuffer(buffer, recipe);
	}

	@Override
	public final T read(ResourceLocation id, PacketBuffer buffer) {
		return readFromBuffer(id, buffer);
	}

	public ProcessingRecipeFactory<T> getFactory() {
		return factory;
	}

}
