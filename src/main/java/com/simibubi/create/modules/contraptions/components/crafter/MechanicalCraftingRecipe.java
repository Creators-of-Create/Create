package com.simibubi.create.modules.contraptions.components.crafter;

import com.google.gson.JsonObject;
import com.simibubi.create.AllRecipes;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class MechanicalCraftingRecipe extends ShapedRecipe {

	public MechanicalCraftingRecipe(ResourceLocation idIn, String groupIn, int recipeWidthIn, int recipeHeightIn,
			NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn) {
		super(idIn, groupIn, recipeWidthIn, recipeHeightIn, recipeItemsIn, recipeOutputIn);
	}

	private static MechanicalCraftingRecipe fromShaped(ShapedRecipe recipe) {
		return new MechanicalCraftingRecipe(recipe.getId(), recipe.getGroup(), recipe.getWidth(), recipe.getHeight(),
				recipe.getIngredients(), recipe.getRecipeOutput());
	}

	@Override
	public boolean matches(CraftingInventory inv, World worldIn) {
		return inv instanceof MechanicalCraftingInventory && super.matches(inv, worldIn);
	}

	@Override
	public IRecipeType<?> getType() {
		return AllRecipes.MECHANICAL_CRAFTING.type;
	}
	
	@Override
	public IRecipeSerializer<?> getSerializer() {
		return AllRecipes.MECHANICAL_CRAFTING.serializer;
	}

	public static class Serializer extends ShapedRecipe.Serializer {

		@Override
		public ShapedRecipe read(ResourceLocation recipeId, JsonObject json) {
			return fromShaped(super.read(recipeId, json));
		}
		
		@Override
		public ShapedRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
			return fromShaped(super.read(recipeId, buffer));
		}

	}

}
