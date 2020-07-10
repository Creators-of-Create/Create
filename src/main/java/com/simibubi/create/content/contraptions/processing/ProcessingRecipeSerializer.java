package com.simibubi.create.content.contraptions.processing;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ProcessingRecipeSerializer<T extends ProcessingRecipe<?>>
	extends net.minecraftforge.registries.ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {

	protected final IRecipeFactory<T> factory;

	public ProcessingRecipeSerializer(IRecipeFactory<T> factory) {
		this.factory = factory;
	}

	@SuppressWarnings("deprecation")
	public T read(ResourceLocation recipeId, JsonObject json) {
		String s = JSONUtils.getString(json, "group", "");

		List<ProcessingIngredient> ingredients = new ArrayList<>();
		List<FluidStack> fluidIngredients = new ArrayList<>();
		for (JsonElement e : JSONUtils.getJsonArray(json, "ingredients")) {
			JsonObject entry = e.getAsJsonObject();
			if (JSONUtils.hasField(entry, "fluid")) {
				Fluid fluid = ForgeRegistries.FLUIDS
					.getValue(ResourceLocation.tryCreate(JSONUtils.getString(entry.get("fluid"), "fluid")));
				int amount = 1;
				if (JSONUtils.hasField((JsonObject) e, "amount")) {
					amount = JSONUtils.getInt(entry.get("amount"), "amount");
				}
				if (fluid != null && amount > 0)
					fluidIngredients.add(new FluidStack(fluid, amount));
			} else {
				int count = 1;
				if (JSONUtils.hasField((JsonObject) e, "count")) {
					count = JSONUtils.getInt(entry.get("count"), "count");
				}
				for (int i = 0; i < count; i++) {
					ingredients.add(ProcessingIngredient.parse(entry));
				}
			}
		}

		List<ProcessingOutput> results = new ArrayList<>();
		List<FluidStack> fluidResults = new ArrayList<>();
		for (JsonElement e : JSONUtils.getJsonArray(json, "results")) {
			JsonObject entry = e.getAsJsonObject();
			if (JSONUtils.hasField(entry, "fluid")) {
				Fluid fluid = ForgeRegistries.FLUIDS
					.getValue(ResourceLocation.tryCreate(JSONUtils.getString(entry.get("fluid"), "fluid")));
				int amount = 1;
				if (JSONUtils.hasField((JsonObject) e, "amount")) {
					amount = JSONUtils.getInt(entry.get("amount"), "amount");
				}
				if (fluid != null && amount > 0)
					fluidResults.add(new FluidStack(fluid, amount));
			} else {
				String s1 = JSONUtils.getString(entry.get("item"), "item");
				int i = JSONUtils.getInt(entry.get("count"), "count");
				float chance = 1;
				if (JSONUtils.hasField((JsonObject) e, "chance"))
					chance = JSONUtils.getFloat(entry.get("chance"), "chance");
				ItemStack itemstack = new ItemStack(Registry.ITEM.getOrDefault(new ResourceLocation(s1)), i);
				results.add(new ProcessingOutput(itemstack, chance));
			}
		}

		int duration = -1;
		if (JSONUtils.hasField(json, "processingTime"))
			duration = JSONUtils.getInt(json, "processingTime");

		return this.factory.create(recipeId, s, ingredients, results, duration, fluidIngredients, fluidResults);
	}

	public T read(ResourceLocation recipeId, PacketBuffer buffer) {
		String s = buffer.readString(32767);

		List<ProcessingIngredient> ingredients = new ArrayList<>();
		int ingredientCount = buffer.readInt();
		for (int i = 0; i < ingredientCount; i++)
			ingredients.add(ProcessingIngredient.parse(buffer));

		int fluidInputCount = buffer.readInt();
		List<FluidStack> fluidIngredients = new ArrayList<>();
		for (int i = 0; i < fluidInputCount; i++)
			fluidIngredients.add(FluidStack.readFromPacket(buffer));

		List<ProcessingOutput> results = new ArrayList<>();
		int outputCount = buffer.readInt();
		for (int i = 0; i < outputCount; i++)
			results.add(ProcessingOutput.read(buffer));

		int fluidOutputCount = buffer.readInt();
		List<FluidStack> fluidResults = new ArrayList<>();
		for (int i = 0; i < fluidOutputCount; i++)
			fluidResults.add(FluidStack.readFromPacket(buffer));

		int duration = buffer.readInt();

		return this.factory.create(recipeId, s, ingredients, results, duration, fluidIngredients, fluidResults);
	}

	public void write(PacketBuffer buffer, T recipe) {
		buffer.writeString(recipe.group);

		buffer.writeInt(recipe.ingredients.size());
		recipe.ingredients.forEach(i -> i.write(buffer));
		if (recipe.canHaveFluidIngredient() && recipe.fluidIngredients != null) {
			buffer.writeInt(recipe.fluidIngredients.size());
			recipe.fluidIngredients.forEach(fluidStack -> fluidStack.writeToPacket(buffer));
		} else {
			buffer.writeInt(0);
		}

		buffer.writeInt(recipe.getRollableItemResults()
			.size());
		recipe.getRollableItemResults()
			.forEach(i -> i.write(buffer));
		if (recipe.canHaveFluidOutput() && recipe.fluidResults != null) {
			buffer.writeInt(recipe.fluidResults.size());
			recipe.fluidResults.forEach(fluidStack -> fluidStack.writeToPacket(buffer));
		} else {
			buffer.writeInt(0);
		}

		buffer.writeInt(recipe.processingDuration);
	}

	public interface IRecipeFactory<T extends ProcessingRecipe<?>> {
		T create(ResourceLocation recipeId, String s, List<ProcessingIngredient> ingredients,
			List<ProcessingOutput> results, int duration, List<FluidStack> fluidIngredients,
			List<FluidStack> fluidResults);
	}

}
