package com.simibubi.create.compat.jei;

import java.util.Arrays;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.components.press.PressingRecipe;
import com.simibubi.create.modules.contraptions.processing.StochasticOutput;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class PressingCategory implements IRecipeCategory<PressingRecipe> {

	private AnimatedPress press;
	private static ResourceLocation ID = new ResourceLocation(Create.ID, "pressing");
	private IDrawable icon;

	public PressingCategory() {
		icon = new DoubleItemIcon(() -> new ItemStack(AllBlocks.MECHANICAL_PRESS.get()),
				() -> new ItemStack(AllItems.IRON_SHEET.get()));
		press = new AnimatedPress();
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
	public Class<? extends PressingRecipe> getRecipeClass() {
		return PressingRecipe.class;
	}

	@Override
	public String getTitle() {
		return Lang.translate("recipe.pressing");
	}

	@Override
	public IDrawable getBackground() {
		return new ScreenResourceWrapper(ScreenResources.PRESSER_RECIPE);
	}

	@Override
	public void setIngredients(PressingRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutputs(VanillaTypes.ITEM, recipe.getPossibleOutputs());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, PressingRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		itemStacks.init(0, true, 27, 60);
		itemStacks.set(0, Arrays.asList(recipe.getIngredients().get(0).getMatchingStacks()));

		List<StochasticOutput> results = recipe.getRollableResults();
		for (int outputIndex = 0; outputIndex < results.size(); outputIndex++) {
			itemStacks.init(outputIndex + 1, false, 113 + 19 * outputIndex, 60);
			itemStacks.set(outputIndex + 1, results.get(outputIndex).getStack());
		}
	}
	
	@Override
	public void draw(PressingRecipe recipe, double mouseX, double mouseY) {
		press.draw(ScreenResources.PRESSER_RECIPE.width / 2, 30);
		
	}

}
