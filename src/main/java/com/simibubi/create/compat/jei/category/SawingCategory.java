package com.simibubi.create.compat.jei.category;

import java.util.Arrays;
import java.util.List;

import com.simibubi.create.AllBlocksNew;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.compat.jei.category.animations.AnimatedSaw;
import com.simibubi.create.modules.contraptions.components.saw.CuttingRecipe;
import com.simibubi.create.modules.contraptions.processing.ProcessingOutput;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.Items;

public class SawingCategory extends CreateRecipeCategory<CuttingRecipe> {

	private AnimatedSaw saw = new AnimatedSaw();

	public SawingCategory() {
		super("sawing", doubleItemIcon(AllBlocksNew.SAW.get(), Items.OAK_LOG), emptyBackground(177, 70));
	}

	@Override
	public Class<? extends CuttingRecipe> getRecipeClass() {
		return CuttingRecipe.class;
	}

	@Override
	public void setIngredients(CuttingRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutputs(VanillaTypes.ITEM, recipe.getPossibleOutputs());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, CuttingRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		itemStacks.init(0, true, 43, 4);
		itemStacks.set(0, Arrays.asList(recipe.getIngredients().get(0).getMatchingStacks()));

		List<ProcessingOutput> results = recipe.getRollableResults();
		for (int outputIndex = 0; outputIndex < results.size(); outputIndex++) {
			int xOffset = outputIndex % 2 == 0 ? 0 : 19;
			int yOffset = (outputIndex / 2) * -19;

			itemStacks.init(outputIndex + 1, false, 117 + xOffset, 47 + yOffset);
			itemStacks.set(outputIndex + 1, results.get(outputIndex).getStack());
		}

		addStochasticTooltip(itemStacks, results);
	}

	@Override
	public void draw(CuttingRecipe recipe, double mouseX, double mouseY) {
		ScreenResources.JEI_SLOT.draw(43, 4);
		int size = recipe.getRollableResults().size();
		for (int i = 0; i < size; i++) {
			int xOffset = i % 2 == 0 ? 0 : 19;
			int yOffset = (i / 2) * -19;
			getRenderedSlot(recipe, i).draw(117 + xOffset, 47 + yOffset);
		}
		ScreenResources.JEI_DOWN_ARROW.draw(70, 6);
		saw.draw(72, 42);
	}

}
