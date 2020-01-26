package com.simibubi.create.compat.jei;

import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.foundation.gui.ScreenElementRenderer;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.components.fan.SplashingRecipe;
import com.simibubi.create.modules.contraptions.processing.ProcessingOutput;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;

public class SplashingCategory extends ProcessingViaFanCategory<SplashingRecipe> {

	private static ResourceLocation ID = new ResourceLocation(Create.ID, "splashing");
	private IDrawable icon;
	private IDrawable background = new EmptyBackground(177, 70);

	public SplashingCategory() {
		icon = new DoubleItemIcon(() -> new ItemStack(AllItems.PROPELLER.get()),
				() -> new ItemStack(Items.WATER_BUCKET));
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public ResourceLocation getUid() {
		return ID;
	}

	@Override
	public Class<? extends SplashingRecipe> getRecipeClass() {
		return SplashingRecipe.class;
	}

	@Override
	public String getTitle() {
		return Lang.translate("recipe.splashing");
	}

	@Override
	public void setIngredients(SplashingRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutputs(VanillaTypes.ITEM, recipe.getPossibleOutputs());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, SplashingRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		itemStacks.init(0, true, 20, 47);
		itemStacks.set(0, Arrays.asList(recipe.getIngredients().get(0).getMatchingStacks()));

		List<ProcessingOutput> results = recipe.getRollableResults();
		boolean single = results.size() == 1;
		for (int outputIndex = 0; outputIndex < results.size(); outputIndex++) {
			int xOffset = outputIndex % 2 == 0 ? 0 : 19;
			int yOffset = (outputIndex / 2) * -19;

			itemStacks.init(outputIndex + 1, false, single ? 139 : 133 + xOffset, 47 + yOffset);
			itemStacks.set(outputIndex + 1, results.get(outputIndex).getStack());
		}

		CreateJEI.addStochasticTooltip(itemStacks, results);
	}

	@Override
	protected void renderWidgets(SplashingRecipe recipe, double mouseX, double mouseY) {
		int size = recipe.getPossibleOutputs().size();

		ScreenResources.JEI_SLOT.draw(20, 47);
		ScreenResources.JEI_SHADOW.draw(47, 29);
		ScreenResources.JEI_SHADOW.draw(66, 39);
		ScreenResources.JEI_LONG_ARROW.draw(53, 51);

		if (size > 1) {
			for (int i = 0; i < size; i++) {
				int xOffset = i % 2 == 0 ? 0 : 19;
				int yOffset = (i / 2) * -19;
				ScreenResources.JEI_SLOT.draw(133 + xOffset, 47 + yOffset);
			}
		} else {
			ScreenResources.JEI_SLOT.draw(139, 47);
		}
	}

	@Override
	public void renderAttachedBlock() {
		BlockState state = Blocks.WATER.getDefaultState().with(FlowingFluidBlock.LEVEL, 8);
		GlStateManager.pushMatrix();
		GlStateManager.translated(0, 0, 200);

		GlStateManager.pushMatrix();
		GlStateManager.translated(0, 200, 0);
		GlStateManager.rotated(90, 1, 0, 0);
		ScreenElementRenderer.renderBlock(() -> state);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translated(0, 200, 0);
		GlStateManager.rotated(90, 1, 0, 0);
		GlStateManager.rotated(270, 0, 0, 1);
		ScreenElementRenderer.renderBlock(() -> state);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translated(-103, -100, 0);
		ScreenElementRenderer.renderBlock(() -> state);
		GlStateManager.popMatrix();

		GlStateManager.popMatrix();
	}

}
