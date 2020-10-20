package com.simibubi.create.compat.jei.category;

import java.util.Arrays;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.category.animations.AnimatedPress;
import com.simibubi.create.content.contraptions.processing.BasinRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.block.Blocks;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.NonNullList;

public class PackingCategory extends BasinCategory {

	private AnimatedPress press = new AnimatedPress(true);
	private PackingType type;

	enum PackingType {
		AUTO_SQUARE, COMPACTING;
	}

	public static PackingCategory standard() {
		return new PackingCategory(PackingType.COMPACTING, AllBlocks.BASIN.get(), 103);
	}

	public static PackingCategory autoSquare() {
		return new PackingCategory(PackingType.AUTO_SQUARE, Blocks.CRAFTING_TABLE, 85);
	}

	protected PackingCategory(PackingType type, IItemProvider icon, int height) {
		super(type != PackingType.AUTO_SQUARE, doubleItemIcon(AllBlocks.MECHANICAL_PRESS.get(), icon),
			emptyBackground(177, height));
		this.type = type;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, BasinRecipe recipe, IIngredients ingredients) {
		if (type == PackingType.COMPACTING) {
			super.setRecipe(recipeLayout, recipe, ingredients);
			return;
		}

		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		int i = 0;

		NonNullList<Ingredient> ingredients2 = recipe.getIngredients();
		int size = ingredients2.size();
		int rows = size == 4 ? 2 : 3;
		while (i < size) {
			Ingredient ingredient = ingredients2.get(i);
			itemStacks.init(i, true, (rows == 2 ? 26 : 17) + (i % rows) * 19, 50 - (i / rows) * 19);
			itemStacks.set(i, Arrays.asList(ingredient.getMatchingStacks()));
			i++;
		}

		itemStacks.init(i, false, 141, 50);
		itemStacks.set(i, recipe.getRecipeOutput());
	}

	@Override
	public void draw(BasinRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
		if (type == PackingType.COMPACTING) {
			super.draw(recipe, matrixStack, mouseX, mouseY);

		} else {
			NonNullList<Ingredient> ingredients2 = recipe.getIngredients();
			int size = ingredients2.size();
			int rows = size == 4 ? 2 : 3;
			for (int i = 0; i < size; i++)
				AllGuiTextures.JEI_SLOT.draw(matrixStack, (rows == 2 ? 26 : 17) + (i % rows) * 19,
					50 - (i / rows) * 19);
			AllGuiTextures.JEI_SLOT.draw(matrixStack, 141, 50);
			AllGuiTextures.JEI_DOWN_ARROW.draw(matrixStack, 136, 32);
			AllGuiTextures.JEI_SHADOW.draw(matrixStack, 81, 68);
		}

		press.draw(matrixStack, getBackground().getWidth() / 2 + 6, 40);
	}

}
