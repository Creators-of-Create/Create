package com.simibubi.create.compat.jei.category;

import java.util.Arrays;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.compat.jei.DoubleItemIcon;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.compat.jei.category.animations.AnimatedCrafter;
import com.simibubi.create.foundation.utility.Lang;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public class MechanicalCraftingCategory implements IRecipeCategory<ShapedRecipe> {

	private AnimatedCrafter crafter;
	private ResourceLocation id;
	private IDrawable icon;
	private IDrawable background;
	private boolean large;

	public MechanicalCraftingCategory(boolean large) {
		this.large = large;
		icon = new DoubleItemIcon(() -> new ItemStack(AllBlocks.MECHANICAL_CRAFTER.get()), () -> ItemStack.EMPTY);
		crafter = new AnimatedCrafter(large);
		id = new ResourceLocation(Create.ID, "mechanical_crafting" + (large ? "_large" : ""));
		background = new EmptyBackground(large ? 177 : 177, large ? 235 : 81);
	}

	public static boolean isSmall(ShapedRecipe recipe) {
		return Math.max((recipe).getWidth(), (recipe).getHeight()) <= 4;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public ResourceLocation getUid() {
		return id;
	}

	@Override
	public String getTitle() {
		return Lang.translate("recipe.mechanical_crafting");
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void setIngredients(ShapedRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, ShapedRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		NonNullList<Ingredient> recipeIngredients = recipe.getIngredients();

		itemStacks.init(0, false, large ? 136 : 141, large ? 196 : 50);
		itemStacks.set(0, recipe.getRecipeOutput().getStack());

		int x = getGridX(recipe);
		int y = getGridY(recipe);
		int size = recipeIngredients.size();
		for (int i = 0; i < size; i++) {
			itemStacks.init(i + 1, true, x + (i % recipe.getWidth()) * 19, y + (i / recipe.getWidth()) * 19);
			itemStacks.set(i + 1, Arrays.asList(recipeIngredients.get(i).getMatchingStacks()));
		}

	}

	public int getGridY(ShapedRecipe recipe) {
		return 3 + (int) (((large ? 9 : 4) - recipe.getRecipeHeight()) * 19 / 2f);
	}

	public int getGridX(ShapedRecipe recipe) {
		return 3 + (int) (((large ? 9 : 4) - recipe.getRecipeWidth()) * 19 / 2f);
	}

	@Override
	public void draw(ShapedRecipe recipe, double mouseX, double mouseY) {
		int x = getGridX(recipe);
		int y = getGridY(recipe);

		for (int row = 0; row < recipe.getHeight(); row++)
			for (int col = 0; col < recipe.getWidth(); col++)
				if (!recipe.getIngredients().get(row * recipe.getWidth() + col).hasNoMatchingItems())
					ScreenResources.JEI_SLOT.draw(x + col * 19, y + row * 19);

		ScreenResources.JEI_SLOT.draw(large ? 136 : 141, large ? 196 : 50);
		if (large)
			ScreenResources.JEI_ARROW.draw(86, 200);
		else
			ScreenResources.JEI_DOWN_ARROW.draw(136, 32);

		ScreenResources.JEI_SHADOW.draw(large ? 20 : 84, large ? 223 : 68);
		crafter.draw(large ? 105 : 185, large ? 189 : -1);
	}

	@Override
	public Class<? extends ShapedRecipe> getRecipeClass() {
		return ShapedRecipe.class;
	}

}
