package com.simibubi.create.compat.jei.category;

import java.util.Arrays;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.compat.jei.DoubleItemIcon;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.compat.jei.category.animations.AnimatedPress;
import com.simibubi.create.foundation.utility.Lang;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public class PackingCategory implements IRecipeCategory<IRecipe<?>> {

	private AnimatedPress press;
	private static ResourceLocation ID = new ResourceLocation(Create.ID, "packing");
	private IDrawable icon;
	private IDrawable background = new EmptyBackground(177, 70);

	public PackingCategory() {
		icon = new DoubleItemIcon(() -> new ItemStack(AllBlocks.MECHANICAL_PRESS.get()),
				() -> new ItemStack(AllBlocks.BASIN.get()));
		press = new AnimatedPress(true);
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
	public Class<? extends IRecipe<?>> getRecipeClass() {
		return ICraftingRecipe.class;
	}

	@Override
	public String getTitle() {
		return Lang.translate("recipe.packing");
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void setIngredients(IRecipe<?> recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, IRecipe<?> recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		int i = 0;

		NonNullList<Ingredient> ingredients2 = recipe.getIngredients();
		int size = ingredients2.size();
		int rows = size == 4 ? 2 : 3;
		while (i < size) {
			Ingredient ingredient = ingredients2.get(i);
			itemStacks.init(i, true, (rows == 2 ? 26 : 17) + (i % rows) * 19, 50 - (i / rows) * 19);
			itemStacks.set(i, Arrays.asList(ingredient.getMatchingStacks()));
			i++;
		}

		itemStacks.init(i, false, 141, 50);
		itemStacks.set(i, recipe.getRecipeOutput());
	}

	@Override
	public void draw(IRecipe<?> recipe, double mouseX, double mouseY) {
		NonNullList<Ingredient> ingredients2 = recipe.getIngredients();
		int size = ingredients2.size();
		int rows = size == 4 ? 2 : 3;
		for (int i = 0; i < size; i++) {
			ScreenResources.JEI_SLOT.draw((rows == 2 ? 26 : 17) + (i % rows) * 19, 50 - (i / rows) * 19);
		}
		ScreenResources.JEI_SLOT.draw(141, 50);
		ScreenResources.JEI_DOWN_ARROW.draw(136, 32);
		ScreenResources.JEI_SHADOW.draw(81, 57);
		press.draw(getBackground().getWidth() / 2 + 20, 8);
	}

}
