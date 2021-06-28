package com.simibubi.create.compat.jei.category;

import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.components.fan.SplashingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.GuiGameElement;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Items;

public class FanWashingCategory extends ProcessingViaFanCategory<SplashingRecipe> {

	public FanWashingCategory() {
		super(185, doubleItemIcon(AllItems.PROPELLER.get(), Items.WATER_BUCKET));
	}

	@Override
	public Class<? extends SplashingRecipe> getRecipeClass() {
		return SplashingRecipe.class;
	}

	@Override
	public void setIngredients(SplashingRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutputs(VanillaTypes.ITEM, recipe.getRollableResultsAsItemStacks());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, SplashingRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		itemStacks.init(0, true, 12, 47);
		itemStacks.set(0, Arrays.asList(recipe.getIngredients()
				.get(0)
				.getMatchingStacks()));

		List<ProcessingOutput> results = recipe.getRollableResults();
		boolean single = results.size() == 1;
		boolean excessive = results.size() > 9;
		for (int outputIndex = 0; outputIndex < results.size(); outputIndex++) {
			int xOffset = (outputIndex % 3) * 19;
			int yOffset = (outputIndex / 3) * -19;

			itemStacks.init(outputIndex + 1, false, single ? 126 : 126 + xOffset, 47 + yOffset + (excessive ? 8 : 0));
			itemStacks.set(outputIndex + 1, results.get(outputIndex)
					.getStack());
		}

		addStochasticTooltip(itemStacks, results);
	}

	@Override
	protected void renderWidgets(MatrixStack matrixStack, SplashingRecipe recipe, double mouseX, double mouseY) {
		int size = recipe.getRollableResultsAsItemStacks()
				.size();

		AllGuiTextures.JEI_SLOT.draw(matrixStack, 12, 47);
		AllGuiTextures.JEI_SHADOW.draw(matrixStack, 47 + 4, 29);
		AllGuiTextures.JEI_SHADOW.draw(matrixStack, 66 + 4, 39);
		AllGuiTextures.JEI_LONG_ARROW.draw(matrixStack, 42, 51);

		if (size == 1) {
			getRenderedSlot(recipe, 0).draw(matrixStack, 126, 47);
			return;
		}

		for (int i = 0; i < size; i++) {
			int xOffset = (i % 3) * 19;
			int yOffset = (i / 3) * -19 + (size > 9 ? 8 : 0);
			getRenderedSlot(recipe, i).draw(matrixStack, 126 + xOffset, 47 + yOffset);
		}
	}
	
	@Override
	protected void translateFan(MatrixStack ms) {
		ms.translate(56 + 4, 33, 0);
	}

	@Override
	public void renderAttachedBlock(MatrixStack matrixStack) {
		matrixStack.push();

		GuiGameElement.of(Fluids.WATER)
				.scale(24)
				.atLocal(0, 0, 2)
				.render(matrixStack);

		matrixStack.pop();
	}

}
