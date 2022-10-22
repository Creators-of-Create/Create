package com.simibubi.create.compat.jei.category;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.compat.jei.category.animations.AnimatedDeployer;
import com.simibubi.create.content.contraptions.components.deployer.DeployerApplicationRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.Lang;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;

@ParametersAreNonnullByDefault
public class DeployingCategory extends CreateRecipeCategory<DeployerApplicationRecipe> {

	private final AnimatedDeployer deployer = new AnimatedDeployer();

	public DeployingCategory(Info<DeployerApplicationRecipe> info) {
		super(info);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, DeployerApplicationRecipe recipe, IFocusGroup focuses) {
		builder
				.addSlot(RecipeIngredientRole.INPUT, 27, 51)
				.setBackground(getRenderedSlot(), -1, -1)
				.addIngredients(recipe.getProcessedItem());
		IRecipeSlotBuilder handItemSlot = builder
				.addSlot(RecipeIngredientRole.INPUT, 51, 5)
				.setBackground(getRenderedSlot(), -1, -1)
				.addIngredients(recipe.getRequiredHeldItem());
		builder
				.addSlot(RecipeIngredientRole.OUTPUT, 132, 51)
				.setBackground(getRenderedSlot(recipe.getRollableResults().get(0)), -1, -1)
				.addItemStack(recipe.getResultItem())
				.addTooltipCallback(addStochasticTooltip(recipe.getRollableResults().get(0)));

		if (recipe.shouldKeepHeldItem()) {
			handItemSlot.addTooltipCallback((recipeSlotView, tooltip) -> tooltip.add(1, Lang.translateDirect("recipe.deploying.not_consumed").withStyle(ChatFormatting.GOLD)));
		}

	}

	@Override
	public void draw(DeployerApplicationRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack matrixStack, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SHADOW.render(matrixStack, 62, 57);
		AllGuiTextures.JEI_DOWN_ARROW.render(matrixStack, 126, 29);
		deployer.draw(matrixStack, getBackground().getWidth() / 2 - 13, 22);
	}

}
