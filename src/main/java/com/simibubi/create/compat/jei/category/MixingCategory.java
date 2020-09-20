package com.simibubi.create.compat.jei.category;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableInt;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.category.animations.AnimatedBlazeBurner;
import com.simibubi.create.compat.jei.category.animations.AnimatedMixer;
import com.simibubi.create.content.contraptions.components.mixer.MixingRecipe;
import com.simibubi.create.content.contraptions.processing.HeatCondition;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.Pair;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;

public class MixingCategory extends CreateRecipeCategory<MixingRecipe> {

	private AnimatedMixer mixer = new AnimatedMixer();
	private AnimatedBlazeBurner heater = new AnimatedBlazeBurner();

	public MixingCategory() {
		super("mixing", doubleItemIcon(AllBlocks.MECHANICAL_MIXER.get(), AllBlocks.BASIN.get()),
			emptyBackground(177, 110));
	}

	@Override
	public Class<? extends MixingRecipe> getRecipeClass() {
		return MixingRecipe.class;
	}

	@Override
	public void setIngredients(MixingRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, MixingRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		NonNullList<Ingredient> recipeIngredients = recipe.getIngredients();
		List<Pair<Ingredient, MutableInt>> actualIngredients = ItemHelper.condenseIngredients(recipeIngredients);

		int size = actualIngredients.size();
		int xOffset = size < 3 ? (3 - size) * 19 / 2 : 0;
		int yOffset = recipe.getRequiredHeat() != HeatCondition.NONE ? 30 : 10;

		int i;
		for (i = 0; i < actualIngredients.size(); i++) {
			itemStacks.init(i, true, 16 + xOffset + (i % 3) * 19, 50 - (i / 3) * 19 + yOffset);
			List<ItemStack> stacks = new ArrayList<>();
			Pair<Ingredient, MutableInt> pair = actualIngredients.get(i);
			Ingredient ingredient = pair.getFirst();
			MutableInt amount = pair.getSecond();

			for (ItemStack itemStack : ingredient.getMatchingStacks()) {
				ItemStack stack = itemStack.copy();
				stack.setCount(amount.getValue());
				stacks.add(stack);
			}

			itemStacks.set(i, stacks);
		}

		itemStacks.init(i, false, 141, 50 + yOffset);
		itemStacks.set(i, recipe.getRecipeOutput()
			.getStack());
	}

	@Override
	public void draw(MixingRecipe recipe, double mouseX, double mouseY) {
		List<Pair<Ingredient, MutableInt>> actualIngredients = ItemHelper.condenseIngredients(recipe.getIngredients());

		int size = actualIngredients.size();
		int xOffset = size < 3 ? (3 - size) * 19 / 2 : 0;
		HeatCondition requiredHeat = recipe.getRequiredHeat();
		int yOffset = requiredHeat != HeatCondition.NONE ? 30 : 10;
		for (int i = 0; i < size; i++)
			AllGuiTextures.JEI_SLOT.draw(16 + xOffset + (i % 3) * 19, 50 - (i / 3) * 19 + yOffset);

		AllGuiTextures.JEI_SLOT.draw(141, 50 + yOffset);
		AllGuiTextures.JEI_DOWN_ARROW.draw(136, 32 + yOffset);
		AllGuiTextures.JEI_SHADOW.draw(81, 57 + yOffset);

		if (requiredHeat != HeatCondition.NONE)
			heater.withHeat(requiredHeat.visualizeAsBlazeBurner())
				.draw(getBackground().getWidth() / 2 + 3, 55);
		mixer.draw(getBackground().getWidth() / 2 + 3, 34);
	}

}
