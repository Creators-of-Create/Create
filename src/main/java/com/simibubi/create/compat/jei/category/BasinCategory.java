package com.simibubi.create.compat.jei.category;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import org.apache.commons.lang3.mutable.MutableInt;

import com.simibubi.create.content.contraptions.processing.BasinRecipe;
import com.simibubi.create.content.contraptions.processing.HeatCondition;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.Pair;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;

public class BasinCategory extends CreateRecipeCategory<BasinRecipe> {

	public BasinCategory(String id, IDrawable icon, IDrawable background) {
		super(id, icon, background);
	}

	@Override
	public Class<? extends BasinRecipe> getRecipeClass() {
		return BasinRecipe.class;
	}

	@Override
	public void setIngredients(BasinRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, BasinRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		NonNullList<Ingredient> recipeIngredients = recipe.getIngredients();
		List<Pair<Ingredient, MutableInt>> actualIngredients = ItemHelper.condenseIngredients(recipeIngredients);

		int size = actualIngredients.size();
		int xOffset = size < 3 ? (3 - size) * 19 / 2 : 0;
		int yOffset = recipe.getRequiredHeat() != HeatCondition.NONE ? 30 : 10;

		int i;
		for (i = 0; i < actualIngredients.size(); i++) {
			itemStacks.init(i, true, 16 + xOffset + (i % 3) * 19, 50 - (i / 3) * 19 + yOffset);
			List<ItemStack> stacks = new ArrayList<>();
			Pair<Ingredient, MutableInt> pair = actualIngredients.get(i);
			Ingredient ingredient = pair.getFirst();
			MutableInt amount = pair.getSecond();

			for (ItemStack itemStack : ingredient.getMatchingStacks()) {
				ItemStack stack = itemStack.copy();
				stack.setCount(amount.getValue());
				stacks.add(stack);
			}

			itemStacks.set(i, stacks);
		}

		itemStacks.init(i, false, 141, 50 + yOffset);
		itemStacks.set(i, recipe.getRecipeOutput()
			.getStack());
	}

	@Override
	public void draw(BasinRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
		List<Pair<Ingredient, MutableInt>> actualIngredients = ItemHelper.condenseIngredients(recipe.getIngredients());

		int size = actualIngredients.size();
		int xOffset = size < 3 ? (3 - size) * 19 / 2 : 0;
		HeatCondition requiredHeat = recipe.getRequiredHeat();
		int yOffset = requiredHeat != HeatCondition.NONE ? 30 : 10;
		for (int i = 0; i < size; i++)
			AllGuiTextures.JEI_SLOT.draw(matrixStack, 16 + xOffset + (i % 3) * 19, 50 - (i / 3) * 19 + yOffset);

		AllGuiTextures.JEI_SLOT.draw(matrixStack, 141, 50 + yOffset);
		AllGuiTextures.JEI_DOWN_ARROW.draw(matrixStack, 136, 32 + yOffset);
		AllGuiTextures.JEI_SHADOW.draw(matrixStack, 81, 57 + yOffset);
	}

}
