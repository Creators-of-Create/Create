package com.simibubi.create.compat.rei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.rei.category.animations.AnimatedKinetics;
import com.simibubi.create.compat.rei.display.FanWashingDisplay;
import com.simibubi.create.content.contraptions.components.fan.SplashingRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.element.GuiGameElement;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

public class FanWashingCategory extends ProcessingViaFanCategory<SplashingRecipe, FanWashingDisplay> {

	public FanWashingCategory() {
		super(185, doubleItemIcon(AllItems.PROPELLER, () -> Items.WATER_BUCKET));
	}

//	@Override
//	public Class<? extends SplashingRecipe> getRecipeClass() {
//		return SplashingRecipe.class;
//	}
//
//	@Override
//	public void setIngredients(SplashingRecipe recipe, IIngredients ingredients) {
//		ingredients.setInputIngredients(recipe.getIngredients());
//		ingredients.setOutputs(VanillaTypes.ITEM, recipe.getRollableResultsAsItemStacks());
//	}

//	@Override
//	public void setRecipe(IRecipeLayout recipeLayout, SplashingRecipe recipe, IIngredients ingredients) {
//		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
//		List<ProcessingOutput> results = recipe.getRollableResults();
//		int xOffsetGlobal = 8 * (3 - Math.min(3, results.size()));
//
//		itemStacks.init(0, true, xOffsetGlobal + 12, 47);
//		itemStacks.set(0, Arrays.asList(recipe.getIngredients()
//			.get(0)
//			.getItems()));
//
//		boolean single = results.size() == 1;
//		boolean excessive = results.size() > 9;
//		for (int outputIndex = 0; outputIndex < results.size(); outputIndex++) {
//			int xOffset = (outputIndex % 3) * 19;
//			int yOffset = (outputIndex / 3) * -19;
//
//			itemStacks.init(outputIndex + 1, false, xOffsetGlobal + (single ? 126 : 126 + xOffset),
//				47 + yOffset + (excessive ? 8 : 0));
//			itemStacks.set(outputIndex + 1, results.get(outputIndex)
//				.getStack());
//		}
//
//		addStochasticTooltip(itemStacks, results);
//	}

	@Override
	protected void renderWidgets(PoseStack matrixStack, SplashingRecipe recipe, double mouseX, double mouseY) {
		int size = recipe.getRollableResultsAsItemStacks()
			.size();
		int xOffsetGlobal = 8 * (3 - Math.min(3, size));

		AllGuiTextures.JEI_SLOT.render(matrixStack, xOffsetGlobal + 12, 47);
		AllGuiTextures.JEI_SHADOW.render(matrixStack, 47 + 4, 29);
		AllGuiTextures.JEI_SHADOW.render(matrixStack, 66 + 4, 39);
		AllGuiTextures.JEI_LONG_ARROW.render(matrixStack, xOffsetGlobal + 42, 51);

		if (size == 1) {
			getRenderedSlot(recipe, 0).render(matrixStack, xOffsetGlobal + 126, 47);
			return;
		}

		for (int i = 0; i < size; i++) {
			int xOffset = (i % 3) * 19;
			int yOffset = (i / 3) * -19 + (size > 9 ? 8 : 0);
			getRenderedSlot(recipe, i).render(matrixStack, xOffsetGlobal + 126 + xOffset, 47 + yOffset);
		}
	}

	@Override
	protected void translateFan(PoseStack ms) {
		ms.translate(56 + 4, 33, 0);
	}

	@Override
	public void renderAttachedBlock(PoseStack matrixStack) {
		matrixStack.pushPose();

		GuiGameElement.of(Fluids.WATER)
			.scale(24)
			.atLocal(0, 0, 2)
			.lighting(AnimatedKinetics.DEFAULT_LIGHTING)
			.render(matrixStack);

		matrixStack.popPose();
	}

}
