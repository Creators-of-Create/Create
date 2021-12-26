package com.simibubi.create.compat.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.category.animations.AnimatedDeployer;
import com.simibubi.create.compat.jei.display.DeployingDisplay;
import com.simibubi.create.content.contraptions.components.deployer.DeployerApplicationRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;

public class DeployingCategory extends CreateRecipeCategory<DeployerApplicationRecipe, DeployingDisplay> {

	AnimatedDeployer deployer;

	public DeployingCategory() {
		super(itemIcon(AllBlocks.DEPLOYER.get()), emptyBackground(177, 70));
		deployer = new AnimatedDeployer();
	}

//	@Override
//	public Class<DeployerApplicationRecipe> getRecipeClass() {
//		return DeployerApplicationRecipe.class;
//	}
//
//	@Override
//	public void setIngredients(DeployerApplicationRecipe recipe, IIngredients ingredients) {
//		ingredients.setInputIngredients(recipe.getIngredients());
//		ingredients.setInputLists(VanillaTypes.FLUID, recipe.getFluidIngredients()
//			.stream()
//			.map(FluidIngredient::getMatchingFluidStacks)
//			.collect(Collectors.toList()));
//
//		if (!recipe.getRollableResults()
//			.isEmpty())
//			ingredients.setOutput(VanillaTypes.ITEM, recipe.getResultItem());
//	}
//
//	@Override
//	public void setRecipe(IRecipeLayout recipeLayout, DeployerApplicationRecipe recipe, IIngredients ingredients) {
//		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
//		itemStacks.init(0, true, 26, 50);
//		itemStacks.set(0, Arrays.asList(recipe.getProcessedItem()
//			.getItems()));
//		itemStacks.init(1, true, 50, 4);
//		itemStacks.set(1, Arrays.asList(recipe.getRequiredHeldItem()
//			.getItems()));
//		itemStacks.init(2, false, 131, 50);
//		itemStacks.set(2, recipe.getResultItem());
//
//		if (recipe.shouldKeepHeldItem()) {
//			itemStacks.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
//				if (!input)
//					return;
//				if (slotIndex != 1)
//					return;
//				tooltip.add(1, Lang.translate("recipe.deploying.not_consumed")
//					.withStyle(ChatFormatting.GOLD));
//			});
//		}
//
//		addStochasticTooltip(itemStacks, recipe.getRollableResults(), 2);
//	}

	@Override
	public void draw(DeployerApplicationRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SLOT.render(matrixStack, 50, 4);
		AllGuiTextures.JEI_SLOT.render(matrixStack, 26, 50);
		getRenderedSlot(recipe, 0).render(matrixStack, 131, 50);
		AllGuiTextures.JEI_SHADOW.render(matrixStack, 62, 57);
		AllGuiTextures.JEI_DOWN_ARROW.render(matrixStack, 126, 29);
		deployer.draw(matrixStack, /*getBackground().getWidth() / 2 - */13, 22);
	}

}
