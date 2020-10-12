package com.simibubi.create.compat.jei.category;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableInt;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.processing.BasinRecipe;
import com.simibubi.create.content.contraptions.processing.HeatCondition;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.Pair;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;

public class BasinCategory extends CreateRecipeCategory<BasinRecipe> {

	public BasinCategory(String id, IDrawable icon, IDrawable background) {
		super(id, icon, background);
	}

	@Override
	public Class<? extends BasinRecipe> getRecipeClass() {
		return BasinRecipe.class;
	}

	@Override
	public void setIngredients(BasinRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setInputLists(VanillaTypes.FLUID, recipe.getFluidIngredients()
			.stream()
			.map(FluidIngredient::getMatchingFluidStacks)
			.collect(Collectors.toList()));
		if (!recipe.getRollableResults()
			.isEmpty())
			ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
		if (!recipe.getFluidResults()
			.isEmpty())
			ingredients.setOutputs(VanillaTypes.FLUID, recipe.getFluidResults());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, BasinRecipe recipe, IIngredients iingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();

		ItemStack itemOutput = recipe.getRollableResultsAsItemStacks()
			.isEmpty() ? ItemStack.EMPTY
				: recipe.getRollableResultsAsItemStacks()
					.get(0);
		FluidStack fluidOutput = recipe.getFluidResults()
			.isEmpty() ? FluidStack.EMPTY
				: recipe.getFluidResults()
					.get(0);

		NonNullList<FluidIngredient> fluidIngredients = recipe.getFluidIngredients();
		List<Pair<Ingredient, MutableInt>> ingredients = ItemHelper.condenseIngredients(recipe.getIngredients());

		int size = ingredients.size() + fluidIngredients.size();
		int xOffset = size < 3 ? (3 - size) * 19 / 2 : 0;
		int yOffset = recipe.getRequiredHeat() != HeatCondition.NONE ? 30 : 10;

		int i;
		for (i = 0; i < ingredients.size(); i++) {
			itemStacks.init(i, true, 16 + xOffset + (i % 3) * 19, 50 - (i / 3) * 19 + yOffset);
			List<ItemStack> stacks = new ArrayList<>();
			Pair<Ingredient, MutableInt> pair = ingredients.get(i);
			Ingredient ingredient = pair.getFirst();
			MutableInt amount = pair.getSecond();

			for (ItemStack itemStack : ingredient.getMatchingStacks()) {
				ItemStack stack = itemStack.copy();
				stack.setCount(amount.getValue());
				stacks.add(stack);
			}

			itemStacks.set(i, stacks);
		}

		int j;
		for (j = 0; j < fluidIngredients.size(); j++) {
			int i2 = i + j;
			fluidStacks.init(j, true, 17 + xOffset + (i2 % 3) * 19, 51 - (i2 / 3) * 19 + yOffset);
			List<FluidStack> stacks = fluidIngredients.get(j)
				.getMatchingFluidStacks();
			fluidStacks.set(j, stacks);
		}

		if (!itemOutput.isEmpty()) {
			itemStacks.init(i, false, 141, 50 + yOffset);
			itemStacks.set(i, recipe.getRecipeOutput()
				.getStack());
			yOffset -= 19;
		}

		if (!fluidOutput.isEmpty()) {
			fluidStacks.init(j, false, 142, 51 + yOffset);
			fluidStacks.set(j, fluidOutput);
		}
	}

	@Override
	public void draw(BasinRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
		List<Pair<Ingredient, MutableInt>> actualIngredients = ItemHelper.condenseIngredients(recipe.getIngredients());

		int size = actualIngredients.size() + recipe.getFluidIngredients().size();
		int xOffset = size < 3 ? (3 - size) * 19 / 2 : 0;
		HeatCondition requiredHeat = recipe.getRequiredHeat();
		int yOffset = requiredHeat != HeatCondition.NONE ? 30 : 10;
		for (int i = 0; i < size; i++)
			AllGuiTextures.JEI_SLOT.draw(matrixStack, 16 + xOffset + (i % 3) * 19, 50 - (i / 3) * 19 + yOffset);

		AllGuiTextures.JEI_SLOT.draw(matrixStack, 141, 50 + yOffset);
		AllGuiTextures.JEI_DOWN_ARROW.draw(matrixStack, 136, 32 + yOffset);
		AllGuiTextures.JEI_SHADOW.draw(matrixStack, 81, 57 + yOffset);
	}

}
