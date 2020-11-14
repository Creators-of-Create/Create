package com.simibubi.create.compat.jei.category;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableInt;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.processing.BasinRecipe;
import com.simibubi.create.content.contraptions.processing.HeatCondition;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;

public class BasinCategory extends CreateRecipeCategory<BasinRecipe> {

	private boolean needsHeating;

	public BasinCategory(boolean needsHeating, IDrawable icon, IDrawable background) {
		super(icon, background);
		this.needsHeating = needsHeating;
	}

	@Override
	public Class<? extends BasinRecipe> getRecipeClass() {
		return BasinRecipe.class;
	}

	@Override
	public void setIngredients(BasinRecipe recipe, IIngredients ingredients) {
		List<Ingredient> itemIngredients = new ArrayList<>(recipe.getIngredients());

		HeatCondition requiredHeat = recipe.getRequiredHeat();
		if (!requiredHeat.testBlazeBurner(HeatLevel.NONE))
			itemIngredients.add(Ingredient.fromItems(AllBlocks.BLAZE_BURNER.get()));
		if (!requiredHeat.testBlazeBurner(HeatLevel.KINDLED))
			itemIngredients.add(Ingredient.fromItems(AllItems.BLAZE_CAKE.get()));

		ingredients.setInputIngredients(itemIngredients);
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
		int yOffset = 0;

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
			fluidStacks.set(j, withImprovedVisibility(stacks));
		}

		if (!itemOutput.isEmpty()) {
			itemStacks.init(i, false, 141, 50 + yOffset);
			itemStacks.set(i, recipe.getRecipeOutput()
				.getStack());
			yOffset -= 19;
		}

		if (!fluidOutput.isEmpty()) {
			fluidStacks.init(j, false, 142, 51 + yOffset);
			fluidStacks.set(j, withImprovedVisibility(fluidOutput));
		}

		addFluidTooltip(fluidStacks, fluidIngredients, ImmutableList.of(fluidOutput));

		HeatCondition requiredHeat = recipe.getRequiredHeat();
		if (!requiredHeat.testBlazeBurner(HeatLevel.NONE)) {
			itemStacks.init(++i, true, 133, 80);
			itemStacks.set(i, AllBlocks.BLAZE_BURNER.asStack());
		}
		if (!requiredHeat.testBlazeBurner(HeatLevel.KINDLED)) {
			itemStacks.init(++i, true, 152, 80);
			itemStacks.set(i, AllItems.BLAZE_CAKE.asStack());
		}
	}

	@Override
	public void draw(BasinRecipe recipe, double mouseX, double mouseY) {
		List<Pair<Ingredient, MutableInt>> actualIngredients = ItemHelper.condenseIngredients(recipe.getIngredients());

		int size = actualIngredients.size() + recipe.getFluidIngredients()
			.size();
		int xOffset = size < 3 ? (3 - size) * 19 / 2 : 0;
		HeatCondition requiredHeat = recipe.getRequiredHeat();
		int yOffset = 0;

		for (int i = 0; i < size; i++)
			AllGuiTextures.JEI_SLOT.draw(16 + xOffset + (i % 3) * 19, 50 - (i / 3) * 19 + yOffset);

		boolean noHeat = requiredHeat == HeatCondition.NONE;
		AllGuiTextures.JEI_SLOT.draw(141, 50 + yOffset);
		AllGuiTextures.JEI_DOWN_ARROW.draw(136, 32 + yOffset);

		AllGuiTextures shadow = noHeat ? AllGuiTextures.JEI_SHADOW : AllGuiTextures.JEI_LIGHT;
		shadow.draw(81, 58 + (noHeat ? 10 : 30));

		if (!needsHeating)
			return;
		
		AllGuiTextures heatBar = noHeat ? AllGuiTextures.JEI_NO_HEAT_BAR : AllGuiTextures.JEI_HEAT_BAR;
		heatBar.draw(4, 80);
		Minecraft.getInstance().fontRenderer.drawString(Lang.translate(requiredHeat.getTranslationKey()), 9,
			86, requiredHeat.getColor());
	}

}
