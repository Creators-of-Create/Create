package com.simibubi.create.compat.jei.category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.compat.jei.category.BlockCuttingCategory.CondensedBlockCuttingRecipe;
import com.simibubi.create.compat.jei.category.animations.AnimatedSaw;
import com.simibubi.create.foundation.item.ItemHelper;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.StonecuttingRecipe;
import net.minecraft.util.ResourceLocation;

public class BlockCuttingCategory extends CreateRecipeCategory<CondensedBlockCuttingRecipe> {

	private AnimatedSaw saw = new AnimatedSaw();

	public BlockCuttingCategory() {
		super("block_cutting", doubleItemIcon(AllBlocks.SAW.get(), Items.STONE_BRICK_STAIRS), emptyBackground(177, 70));
	}

	@Override
	public Class<? extends CondensedBlockCuttingRecipe> getRecipeClass() {
		return CondensedBlockCuttingRecipe.class;
	}

	@Override
	public void setIngredients(CondensedBlockCuttingRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutputs(VanillaTypes.ITEM, recipe.getOutputs());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, CondensedBlockCuttingRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		itemStacks.init(0, true, 4, 4);
		itemStacks.set(0, Arrays.asList(recipe.getIngredients().get(0).getMatchingStacks()));

		List<ItemStack> results = recipe.getOutputs();
		for (int outputIndex = 0; outputIndex < results.size(); outputIndex++) {
			int xOffset = (outputIndex % 5) * 19;
			int yOffset = (outputIndex / 5) * -19;

			itemStacks.init(outputIndex + 1, false, 77 + xOffset, 47 + yOffset);
			itemStacks.set(outputIndex + 1, results.get(outputIndex).getStack());
		}
	}

	@Override
	public void draw(CondensedBlockCuttingRecipe recipe, double mouseX, double mouseY) {
		ScreenResources.JEI_SLOT.draw(4, 4);
		int size = recipe.getOutputs().size();
		for (int i = 0; i < size; i++) {
			int xOffset = (i % 5) * 19;
			int yOffset = (i / 5) * -19;
			ScreenResources.JEI_SLOT.draw(77 + xOffset, 47 + yOffset);
		}
		ScreenResources.JEI_DOWN_ARROW.draw(31, 6);
		ScreenResources.JEI_SHADOW.draw(19, 55);
		saw.draw(33, 35);
	}

	public static class CondensedBlockCuttingRecipe extends StonecuttingRecipe {

		List<ItemStack> outputs = new ArrayList<>();

		public CondensedBlockCuttingRecipe(Ingredient ingredient) {
			super(new ResourceLocation(""), "", ingredient, ItemStack.EMPTY);
		}

		public void addOutput(ItemStack stack) {
			outputs.add(stack);
		}

		public List<ItemStack> getOutputs() {
			return outputs;
		}

		public static List<CondensedBlockCuttingRecipe> condenseRecipes(List<IRecipe<?>> stoneCuttingRecipes) {
			List<CondensedBlockCuttingRecipe> condensed = new ArrayList<>();
			Recipes: for (IRecipe<?> recipe : stoneCuttingRecipes) {
				Ingredient i1 = recipe.getIngredients().get(0);
				for (CondensedBlockCuttingRecipe condensedRecipe : condensed) {
					if (ItemHelper.matchIngredients(i1, condensedRecipe.getIngredients().get(0))) {
						condensedRecipe.addOutput(recipe.getRecipeOutput());
						continue Recipes;
					}
				}
				CondensedBlockCuttingRecipe cr = new CondensedBlockCuttingRecipe(i1);
				cr.addOutput(recipe.getRecipeOutput());
				condensed.add(cr);
			}
			return condensed;
		}

	}

}
