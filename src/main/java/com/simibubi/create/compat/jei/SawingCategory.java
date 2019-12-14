package com.simibubi.create.compat.jei;

import java.util.Arrays;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.components.saw.CuttingRecipe;
import com.simibubi.create.modules.contraptions.processing.StochasticOutput;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;

public class SawingCategory implements IRecipeCategory<CuttingRecipe> {

	private AnimatedSaw saw;
	private static ResourceLocation ID = new ResourceLocation(Create.ID, "sawing");
	private IDrawable icon;
	private IDrawable background = new EmptyBackground(177, 70);

	public SawingCategory() {
		icon = new DoubleItemIcon(() -> new ItemStack(AllBlocks.SAW.get()), () -> new ItemStack(Items.OAK_LOG));
		saw = new AnimatedSaw();
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
	public Class<? extends CuttingRecipe> getRecipeClass() {
		return CuttingRecipe.class;
	}

	@Override
	public String getTitle() {
		return Lang.translate("recipe.sawing");
	}

	@Override
	public IDrawable getBackground() {
		return background;
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

		List<StochasticOutput> results = recipe.getRollableResults();
		for (int outputIndex = 0; outputIndex < results.size(); outputIndex++) {
			int xOffset = outputIndex % 2 == 0 ? 0 : 19;
			int yOffset = (outputIndex / 2) * -19;

			itemStacks.init(outputIndex + 1, false, 117 + xOffset, 47 + yOffset);
			itemStacks.set(outputIndex + 1, results.get(outputIndex).getStack());
		}
		
		CreateJEI.addStochasticTooltip(itemStacks, results);
	}

	@Override
	public void draw(CuttingRecipe recipe, double mouseX, double mouseY) {
		ScreenResources.JEI_SLOT.draw(43, 4);
		int size = recipe.getRollableResults().size();
		for (int i = 0; i < size; i++) {
			int xOffset = i % 2 == 0 ? 0 : 19;
			int yOffset = (i / 2) * -19;
			ScreenResources.JEI_SLOT.draw(117 + xOffset, 47 + yOffset);
		}
		ScreenResources.JEI_DOWN_ARROW.draw(70, 6);
		ScreenResources.JEI_SHADOW.draw(58, 55);
		saw.draw(72, 35);
	}

}
