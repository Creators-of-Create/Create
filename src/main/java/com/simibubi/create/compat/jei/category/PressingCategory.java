package com.simibubi.create.compat.jei.category;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.compat.jei.category.animations.AnimatedPress;
import com.simibubi.create.content.contraptions.components.press.PressingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;

@ParametersAreNonnullByDefault
public class PressingCategory extends CreateRecipeCategory<PressingRecipe> {

	private final AnimatedPress press = new AnimatedPress(false);

	public PressingCategory(Info<PressingRecipe> info) {
		super(info);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, PressingRecipe recipe, IFocusGroup focuses) {
		builder
				.addSlot(RecipeIngredientRole.INPUT, 27, 51)
				.setBackground(getRenderedSlot(), -1, -1)
				.addIngredients(recipe.getIngredients().get(0));

		List<ProcessingOutput> results = recipe.getRollableResults();
		int i = 0;
		for (ProcessingOutput output : results) {
			builder.addSlot(RecipeIngredientRole.OUTPUT, 131 + 19 * i, 50)
					.setBackground(getRenderedSlot(output), -1, -1)
					.addItemStack(output.getStack())
					.addTooltipCallback(addStochasticTooltip(output));
			i++;
		}
	}

	@Override
	public void draw(PressingRecipe recipe, IRecipeSlotsView iRecipeSlotsView, PoseStack matrixStack,  double mouseX, double mouseY) {
		AllGuiTextures.JEI_SHADOW.render(matrixStack, 61, 41);
		AllGuiTextures.JEI_LONG_ARROW.render(matrixStack, 52, 54);

		press.draw(matrixStack, getBackground().getWidth() / 2 - 17, 22);
	}

}
