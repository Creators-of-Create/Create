package com.simibubi.create.compat.jei.category;

import java.util.Arrays;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.jei.category.animations.AnimatedCrushingWheels;
import com.simibubi.create.content.contraptions.components.crusher.AbstractCrushingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;

public class CrushingCategory extends CreateRecipeCategory<AbstractCrushingRecipe> {

	private AnimatedCrushingWheels crushingWheels = new AnimatedCrushingWheels();

	public CrushingCategory() {
		super("crushing", doubleItemIcon(AllBlocks.CRUSHING_WHEEL.get(), AllItems.CRUSHED_GOLD.get()),
				emptyBackground(177, 100));
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
		itemStacks.init(0, true, 50, 2);
		itemStacks.set(0, Arrays.asList(recipe.getIngredients().get(0).getMatchingStacks()));

		List<ProcessingOutput> results = recipe.getRollableResults();
		int size = results.size();
		int offset = -size * 19 / 2;
		for (int outputIndex = 0; outputIndex < size; outputIndex++) {
			itemStacks.init(outputIndex + 1, false, getBackground().getWidth() / 2 + offset + 19 * outputIndex, 78);
			itemStacks.set(outputIndex + 1, results.get(outputIndex).getStack());
		}

		addStochasticTooltip(itemStacks, results);
	}

	@Override
	public void draw(AbstractCrushingRecipe recipe, double mouseX, double mouseY) {
		List<ProcessingOutput> results = recipe.getRollableResults();
		AllGuiTextures.JEI_SLOT.draw(50, 2);
		AllGuiTextures.JEI_DOWN_ARROW.draw(72, 7);

		int size = results.size();
		int offset = -size * 19 / 2;
		for (int outputIndex = 0; outputIndex < results.size(); outputIndex++)
			getRenderedSlot(recipe, outputIndex).draw(getBackground().getWidth() / 2 + offset + 19 * outputIndex, 78);

		crushingWheels.draw(62, 59);
	}

}
