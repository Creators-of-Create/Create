package com.simibubi.create.compat.jei.category;

import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.jei.category.animations.AnimatedMillstone;
import com.simibubi.create.content.contraptions.components.crusher.AbstractCrushingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;

public class MillingCategory extends CreateRecipeCategory<AbstractCrushingRecipe> {

	private AnimatedMillstone millstone = new AnimatedMillstone();

	public MillingCategory() {
		super(doubleItemIcon(AllBlocks.MILLSTONE.get(), AllItems.WHEAT_FLOUR.get()), emptyBackground(177, 53));
	}

	@Override
	public Class<? extends AbstractCrushingRecipe> getRecipeClass() {
		return AbstractCrushingRecipe.class;
	}

	@Override
	public void setIngredients(AbstractCrushingRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutputs(VanillaTypes.ITEM, recipe.getRollableResultsAsItemStacks());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, AbstractCrushingRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		itemStacks.init(0, true, 14, 8);
		itemStacks.set(0, Arrays.asList(recipe.getIngredients()
			.get(0)
			.getItems()));

		List<ProcessingOutput> results = recipe.getRollableResults();
		boolean single = results.size() == 1;
		for (int outputIndex = 0; outputIndex < results.size(); outputIndex++) {
			int xOffset = outputIndex % 2 == 0 ? 0 : 19;
			int yOffset = (outputIndex / 2) * -19;

			itemStacks.init(outputIndex + 1, false, single ? 139 : 133 + xOffset, 27 + yOffset);
			itemStacks.set(outputIndex + 1, results.get(outputIndex)
				.getStack());
		}

		addStochasticTooltip(itemStacks, results);
	}

	@Override
	public void draw(AbstractCrushingRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
		int size = recipe.getRollableResultsAsItemStacks()
			.size();

		AllGuiTextures.JEI_SLOT.draw(matrixStack, 14, 8);
		AllGuiTextures.JEI_ARROW.draw(matrixStack, 85, 32);
		AllGuiTextures.JEI_DOWN_ARROW.draw(matrixStack, 43, 4);
		millstone.draw(matrixStack, 48, 27);

		if (size == 1) {
			getRenderedSlot(recipe, 0).draw(matrixStack, 139, 27);
			return;
		}

		for (int i = 0; i < size; i++) {
			int xOffset = i % 2 == 0 ? 0 : 19;
			int yOffset = (i / 2) * -19;
			getRenderedSlot(recipe, i).draw(matrixStack, 133 + xOffset, 27 + yOffset);
		}

	}

}
