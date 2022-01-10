package com.simibubi.create.content.contraptions.processing.fan;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder.ProcessingRecipeParams;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeSerializer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

@ParametersAreNonnullByDefault
public class CustomFanProcessingRecipe extends ProcessingRecipe<TypeCustom.CustomRecipeWrapper> {

	public static class Serializer extends ProcessingRecipeSerializer<CustomFanProcessingRecipe> {

		public Serializer() {
			super(CustomFanProcessingRecipe::new);
		}

		@Override
		protected @NotNull CustomFanProcessingRecipe readFromJson(ResourceLocation recipeId, JsonObject json) {
			CustomFanProcessingRecipe recipe = super.readFromJson(recipeId, json);
			AbstractFanProcessingType type = AbstractFanProcessingType.valueOf(json.get("block_type").getAsString());
			if (type instanceof TypeCustom custom) {
				recipe.type = custom;
			} else {
				throw new IllegalArgumentException("recipe " + recipeId + " has invalid block_type " + json.get("block_type").getAsString());
			}
			return recipe;
		}

		@Override
		protected void writeToJson(JsonObject json, CustomFanProcessingRecipe recipe) {
			super.writeToJson(json, recipe);
			json.addProperty("block_type", recipe.type.name());
		}

		@Override
		protected void writeToBuffer(FriendlyByteBuf buffer, CustomFanProcessingRecipe recipe) {
			super.writeToBuffer(buffer, recipe);
			buffer.writeUtf(recipe.type.name());
		}

		@Override
		protected @NotNull CustomFanProcessingRecipe readFromBuffer(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			CustomFanProcessingRecipe recipe = super.readFromBuffer(recipeId, buffer);
			String str = buffer.readUtf();
			AbstractFanProcessingType type = AbstractFanProcessingType.valueOf(str);
			if (type instanceof TypeCustom custom) {
				recipe.type = custom;
			} else {
				throw new IllegalArgumentException("recipe " + recipeId + " has invalid block_type " + str);
			}
			return recipe;
		}
	}

	public AbstractFanProcessingType type = AbstractFanProcessingType.NONE;

	public CustomFanProcessingRecipe(ProcessingRecipeParams params) {
		super(AllRecipeTypes.CUSTOM_FAN, params);
	}

	@Override
	public boolean matches(TypeCustom.CustomRecipeWrapper inv, Level worldIn) {
		if (inv.isEmpty())
			return false;
		if (type == AbstractFanProcessingType.NONE || inv.type != type)
			return false;
		return ingredients.get(0)
				.test(inv.getItem(0));
	}

	@Override
	protected int getMaxInputCount() {
		return 1;
	}

	@Override
	protected int getMaxOutputCount() {
		return 12;
	}

}
