package com.simibubi.create.content.kinetics.crafter;

import com.google.gson.JsonObject;
import com.simibubi.create.AllRecipeTypes;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

public class MechanicalCraftingRecipe extends ShapedRecipe {

	private boolean acceptMirrored;

	public MechanicalCraftingRecipe(ResourceLocation idIn, String groupIn, int recipeWidthIn, int recipeHeightIn,
		NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn, boolean acceptMirrored) {
		super(idIn, groupIn, CraftingBookCategory.MISC, recipeWidthIn, recipeHeightIn, recipeItemsIn, recipeOutputIn);
		this.acceptMirrored = acceptMirrored;
	}

	private static MechanicalCraftingRecipe fromShaped(ShapedRecipe recipe, boolean acceptMirrored) {
		return new MechanicalCraftingRecipe(recipe.getId(), recipe.getGroup(), recipe.getWidth(), recipe.getHeight(),
			recipe.getIngredients(), recipe.getResultItem(), acceptMirrored);
	}

	@Override
	public boolean matches(CraftingContainer inv, Level worldIn) {
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
	private boolean matchesSpecific(CraftingContainer inv, int p_77573_2_, int p_77573_3_) {
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
	public RecipeType<?> getType() {
		return AllRecipeTypes.MECHANICAL_CRAFTING.getType();
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return AllRecipeTypes.MECHANICAL_CRAFTING.getSerializer();
	}

	public boolean acceptsMirrored() {
		return acceptMirrored;
	}

	public static class Serializer extends ShapedRecipe.Serializer {

		@Override
		public ShapedRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			return fromShaped(super.fromJson(recipeId, json), GsonHelper.getAsBoolean(json, "acceptMirrored", true));
		}

		@Override
		public ShapedRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			return fromShaped(super.fromNetwork(recipeId, buffer), buffer.readBoolean() && buffer.readBoolean());
		}

		@Override
		public void toNetwork(FriendlyByteBuf p_199427_1_, ShapedRecipe p_199427_2_) {
			super.toNetwork(p_199427_1_, p_199427_2_);
			if (p_199427_2_ instanceof MechanicalCraftingRecipe) {
				p_199427_1_.writeBoolean(true);
				p_199427_1_.writeBoolean(((MechanicalCraftingRecipe) p_199427_2_).acceptsMirrored());
			} else
				p_199427_1_.writeBoolean(false);
		}

	}

}
