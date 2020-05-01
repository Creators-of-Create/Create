package com.simibubi.create.compat.jei.category;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.compat.jei.category.animations.AnimatedMixer;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.modules.contraptions.components.mixer.MixingRecipe;
import com.simibubi.create.modules.contraptions.processing.ProcessingIngredient;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;

public class MixingCategory extends CreateRecipeCategory<MixingRecipe> {

	private AnimatedMixer mixer = new AnimatedMixer();

	public MixingCategory() {
		super("mixing", doubleItemIcon(AllBlocks.MECHANICAL_MIXER.get(), AllBlocks.BASIN.get()),
				emptyBackground(177, 70));
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

		Map<Integer, Float> catalystIndices = new HashMap<>(9);
		for (int i = 0; i < actualIngredients.size(); i++) {
			for (ProcessingIngredient processingIngredient : recipe.getRollableIngredients()) {
				if (processingIngredient.isCatalyst() && ItemHelper
						.matchIngredients(processingIngredient.getIngredient(), actualIngredients.get(i).getKey())) {
					catalystIndices.put(i, processingIngredient.getOutputChance());
					break;
				}
			}
		}

		int size = actualIngredients.size();
		int xOffset = size < 3 ? (3 - size) * 19 / 2 : 0;
		int i = 0;
		while (i < size) {
			Pair<Ingredient, MutableInt> ingredient = actualIngredients.get(i);
			itemStacks.init(i, true, 16 + xOffset + (i % 3) * 19, 50 - (i / 3) * 19);
			List<ItemStack> asList = Arrays.asList(ingredient.getKey().getMatchingStacks());
			itemStacks.set(i, asList.stream().map(stack -> {
				stack = stack.copy();
				stack.setCount(ingredient.getRight().getValue());
				return stack;
			}).collect(Collectors.toList()));
			i++;
		}

		itemStacks.init(i, false, 141, 50);
		itemStacks.set(i, recipe.getRecipeOutput().getStack());

		addCatalystTooltip(itemStacks, catalystIndices);
	}

	@Override
	public void draw(MixingRecipe recipe, double mouseX, double mouseY) {
		List<Pair<Ingredient, MutableInt>> actualIngredients = ItemHelper.condenseIngredients(recipe.getIngredients());

		int size = actualIngredients.size();
		int xOffset = size < 3 ? (3 - size) * 19 / 2 : 0;
		for (int i = 0; i < size; i++) {
			ScreenResources jeiSlot = ScreenResources.JEI_SLOT;
			for (ProcessingIngredient processingIngredient : recipe.getRollableIngredients()) {
				if (processingIngredient.isCatalyst() && ItemHelper
						.matchIngredients(processingIngredient.getIngredient(), actualIngredients.get(i).getKey())) {
					jeiSlot = ScreenResources.JEI_CATALYST_SLOT;
					break;
				}
			}
			jeiSlot.draw(16 + xOffset + (i % 3) * 19, 50 - (i / 3) * 19);
		}
		ScreenResources.JEI_SLOT.draw(141, 50);
		ScreenResources.JEI_DOWN_ARROW.draw(136, 32);
		ScreenResources.JEI_SHADOW.draw(81, 57);
		mixer.draw(getBackground().getWidth() / 2 + 20, 8);
	}

}
