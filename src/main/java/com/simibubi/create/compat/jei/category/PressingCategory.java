package com.simibubi.create.compat.jei.category;

import java.util.Arrays;
import java.util.List;

import com.simibubi.create.AllBlocksNew;
import com.simibubi.create.AllItems;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.compat.jei.category.animations.AnimatedPress;
import com.simibubi.create.modules.contraptions.components.press.PressingRecipe;
import com.simibubi.create.modules.contraptions.processing.ProcessingOutput;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;

public class PressingCategory extends CreateRecipeCategory<PressingRecipe> {

	private AnimatedPress press = new AnimatedPress(false);

	public PressingCategory() {
		super("pressing", doubleItemIcon(AllBlocksNew.MECHANICAL_PRESS.get(), AllItems.IRON_SHEET.get()),
				emptyBackground(177, 70));
	}

	@Override
	public Class<? extends PressingRecipe> getRecipeClass() {
		return PressingRecipe.class;
	}

	@Override
	public void setIngredients(PressingRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutputs(VanillaTypes.ITEM, recipe.getPossibleOutputs());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, PressingRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		itemStacks.init(0, true, 26, 50);
		itemStacks.set(0, Arrays.asList(recipe.getIngredients().get(0).getMatchingStacks()));

		List<ProcessingOutput> results = recipe.getRollableResults();
		for (int outputIndex = 0; outputIndex < results.size(); outputIndex++) {
			itemStacks.init(outputIndex + 1, false, 131 + 19 * outputIndex, 50);
			itemStacks.set(outputIndex + 1, results.get(outputIndex).getStack());
		}

		addStochasticTooltip(itemStacks, results);
	}

	@Override
	public void draw(PressingRecipe recipe, double mouseX, double mouseY) {
		ScreenResources.JEI_SLOT.draw(26, 50);
		getRenderedSlot(recipe, 0).draw(131, 50);
		if (recipe.getRollableResults().size() > 1)
			getRenderedSlot(recipe, 1).draw(131 + 19, 50);
		ScreenResources.JEI_SHADOW.draw(61, 41);
		ScreenResources.JEI_LONG_ARROW.draw(52, 54);
		press.draw(getBackground().getWidth() / 2 - 17, 22);
	}

}
