package com.simibubi.create.content.contraptions.components.crafter;

import com.google.gson.JsonObject;
import com.simibubi.create.AllRecipeTypes;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class MechanicalCraftingRecipe extends ShapedRecipe {

	private boolean acceptMirrored;

	public MechanicalCraftingRecipe(ResourceLocation idIn, String groupIn, int recipeWidthIn, int recipeHeightIn,
		NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn, boolean acceptMirrored) {
		super(idIn, groupIn, recipeWidthIn, recipeHeightIn, recipeItemsIn, recipeOutputIn);
		this.acceptMirrored = acceptMirrored;
	}

	private static MechanicalCraftingRecipe fromShaped(ShapedRecipe recipe, boolean acceptMirrored) {
		return new MechanicalCraftingRecipe(recipe.getId(), recipe.getGroup(), recipe.getWidth(), recipe.getHeight(),
			recipe.getIngredients(), recipe.getResultItem(), acceptMirrored);
	}

	@Override
	public boolean matches(CraftingInventory inv, World worldIn) {
		if (!(inv instanceof MechanicalCraftingInventory))
			return false;
		if (acceptsMirrored())
			return super.matches(inv, worldIn);

		// From ShapedRecipe except the symmetry
		for (int i = 0; i <= inv.getWidth() - this.getWidth(); ++i)
			for (int j = 0; j <= inv.getHeight() - this.getHeight(); ++j)
				if (this.matchesSpecific(inv, i, j))
					return true;
		return false;
	}

	// From ShapedRecipe
	private boolean matchesSpecific(CraftingInventory inv, int p_77573_2_, int p_77573_3_) {
		NonNullList<Ingredient> ingredients = getIngredients();
		int width = getWidth();
		int height = getHeight();
		for (int i = 0; i < inv.getWidth(); ++i) {
			for (int j = 0; j < inv.getHeight(); ++j) {
				int k = i - p_77573_2_;
				int l = j - p_77573_3_;
				Ingredient ingredient = Ingredient.EMPTY;
				if (k >= 0 && l >= 0 && k < width && l < height)
					ingredient = ingredients.get(k + l * width);
				if (!ingredient.test(inv.getItem(i + j * inv.getWidth())))
					return false;
			}
		}
		return true;
	}

	@Override
	public IRecipeType<?> getType() {
		return AllRecipeTypes.MECHANICAL_CRAFTING.getType();
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return AllRecipeTypes.MECHANICAL_CRAFTING.getSerializer();
	}

	public boolean acceptsMirrored() {
		return acceptMirrored;
	}

	public static class Serializer extends ShapedRecipe.Serializer {

		@Override
		public ShapedRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			return fromShaped(super.fromJson(recipeId, json), JSONUtils.getAsBoolean(json, "acceptMirrored", true));
		}

		@Override
		public ShapedRecipe fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
			return fromShaped(super.fromNetwork(recipeId, buffer), buffer.readBoolean() && buffer.readBoolean());
		}

		@Override
		public void toNetwork(PacketBuffer p_199427_1_, ShapedRecipe p_199427_2_) {
			super.toNetwork(p_199427_1_, p_199427_2_);
			if (p_199427_2_ instanceof MechanicalCraftingRecipe) {
				p_199427_1_.writeBoolean(true);
				p_199427_1_.writeBoolean(((MechanicalCraftingRecipe) p_199427_2_).acceptsMirrored());
			} else
				p_199427_1_.writeBoolean(false);
		}

	}

}
