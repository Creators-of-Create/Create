package com.simibubi.create.compat.jei.category;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.category.animations.AnimatedBlazeBurner;
import com.simibubi.create.compat.jei.category.animations.AnimatedMixer;
import com.simibubi.create.content.contraptions.components.mixer.MixingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingIngredient;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.item.ItemHelper;

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

		Map<Integer, Float> catalystIndices = new HashMap<>(9);
		for (int i = 0; i < actualIngredients.size(); i++) {
			for (ProcessingIngredient processingIngredient : recipe.getRollableIngredients()) {
				if (processingIngredient.isCatalyst()
					&& ItemHelper.matchIngredients(processingIngredient.getIngredient(), actualIngredients.get(i)
						.getKey())) {
					catalystIndices.put(i, processingIngredient.getOutputChance());
					break;
				}
			}
		}

		int size = actualIngredients.size();
		int xOffset = size < 3 ? (3 - size) * 19 / 2 : 0;
		int i = 0;
		int yOffset = recipe.getHeatLevelRequired() > 0 ? 30 : 10;
		while (i < size) {
			Pair<Ingredient, MutableInt> ingredient = actualIngredients.get(i);
			itemStacks.init(i, true, 16 + xOffset + (i % 3) * 19, 50 - (i / 3) * 19 + yOffset);
			List<ItemStack> asList = Arrays.asList(ingredient.getKey()
				.getMatchingStacks());
			itemStacks.set(i, asList.stream()
				.map(stack -> {
					stack = stack.copy();
					stack.setCount(ingredient.getRight()
						.getValue());
					return stack;
				})
				.collect(Collectors.toList()));
			i++;
		}

		itemStacks.init(i, false, 141, 50 + yOffset);
		itemStacks.set(i, recipe.getRecipeOutput()
			.getStack());

		addCatalystTooltip(itemStacks, catalystIndices);
	}

	@Override
	public void draw(MixingRecipe recipe, double mouseX, double mouseY) {
		List<Pair<Ingredient, MutableInt>> actualIngredients = ItemHelper.condenseIngredients(recipe.getIngredients());

		int size = actualIngredients.size();
		int xOffset = size < 3 ? (3 - size) * 19 / 2 : 0;
		int yOffset = recipe.getHeatLevelRequired() > 0 ? 30 : 10;
		for (int i = 0; i < size; i++) {
			AllGuiTextures jeiSlot = AllGuiTextures.JEI_SLOT;
			for (ProcessingIngredient processingIngredient : recipe.getRollableIngredients()) {
				if (processingIngredient.isCatalyst()
					&& ItemHelper.matchIngredients(processingIngredient.getIngredient(), actualIngredients.get(i)
						.getKey())) {
					jeiSlot = AllGuiTextures.JEI_CATALYST_SLOT;
					break;
				}
			}
			jeiSlot.draw(16 + xOffset + (i % 3) * 19, 50 - (i / 3) * 19 + yOffset);
		}
		AllGuiTextures.JEI_SLOT.draw(141, 50 + yOffset);
		AllGuiTextures.JEI_DOWN_ARROW.draw(136, 32 + yOffset);
		AllGuiTextures.JEI_SHADOW.draw(81, 57 + yOffset);
		if (recipe.getHeatLevelRequired() > 0)
			heater.drawWithHeatLevel(getBackground().getWidth() / 2 + 3, 55, recipe.getHeatLevelRequired());
		mixer.draw(getBackground().getWidth() / 2 + 3, 34);
	}

}
