package com.simibubi.create.compat.jei.category;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.category.animations.AnimatedDeployer;
import com.simibubi.create.content.contraptions.components.deployer.DeployerApplicationRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.Lang;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.util.text.TextFormatting;

public class DeployingCategory extends CreateRecipeCategory<DeployerApplicationRecipe> {

	AnimatedDeployer deployer;

	public DeployingCategory() {
		super(itemIcon(AllBlocks.DEPLOYER.get()), emptyBackground(177, 70));
		deployer = new AnimatedDeployer();
	}

	@Override
	public Class<DeployerApplicationRecipe> getRecipeClass() {
		return DeployerApplicationRecipe.class;
	}

	@Override
	public void setIngredients(DeployerApplicationRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setInputLists(VanillaTypes.FLUID, recipe.getFluidIngredients()
			.stream()
			.map(FluidIngredient::getMatchingFluidStacks)
			.collect(Collectors.toList()));

		if (!recipe.getRollableResults()
			.isEmpty())
			ingredients.setOutput(VanillaTypes.ITEM, recipe.getResultItem());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, DeployerApplicationRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		itemStacks.init(0, true, 26, 50);
		itemStacks.set(0, Arrays.asList(recipe.getProcessedItem()
			.getItems()));
		itemStacks.init(1, true, 50, 4);
		itemStacks.set(1, Arrays.asList(recipe.getRequiredHeldItem()
			.getItems()));
		itemStacks.init(2, false, 131, 50);
		itemStacks.set(2, recipe.getResultItem());

		if (recipe.shouldKeepHeldItem()) {
			itemStacks.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
				if (!input)
					return;
				if (slotIndex != 1)
					return;
				tooltip.add(1, Lang.translate("recipe.deploying.not_consumed")
					.withStyle(TextFormatting.GOLD));
			});
		}

		addStochasticTooltip(itemStacks, recipe.getRollableResults(), 2);
	}

	@Override
	public void draw(DeployerApplicationRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SLOT.draw(matrixStack, 50, 4);
		AllGuiTextures.JEI_SLOT.draw(matrixStack, 26, 50);
		getRenderedSlot(recipe, 0).draw(matrixStack, 131, 50);
		AllGuiTextures.JEI_SHADOW.draw(matrixStack, 62, 57);
		AllGuiTextures.JEI_DOWN_ARROW.draw(matrixStack, 126, 29);
		deployer.draw(matrixStack, getBackground().getWidth() / 2 - 13, 22);
	}

}
