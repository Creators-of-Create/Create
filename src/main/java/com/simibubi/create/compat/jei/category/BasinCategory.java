package com.simibubi.create.compat.jei.category;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableInt;

import com.mojang.blaze3d.matrix.MatrixStack;
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
			itemIngredients.add(Ingredient.of(AllBlocks.BLAZE_BURNER.get()));
		if (!requiredHeat.testBlazeBurner(HeatLevel.KINDLED))
			itemIngredients.add(Ingredient.of(AllItems.BLAZE_CAKE.get()));

		ingredients.setInputIngredients(itemIngredients);
		ingredients.setInputLists(VanillaTypes.FLUID, recipe.getFluidIngredients()
			.stream()
			.map(FluidIngredient::getMatchingFluidStacks)
			.collect(Collectors.toList()));
		if (!recipe.getRollableResults()
			.isEmpty())
			ingredients.setOutput(VanillaTypes.ITEM, recipe.getResultItem());
		if (!recipe.getFluidResults()
			.isEmpty())
			ingredients.setOutputs(VanillaTypes.FLUID, recipe.getFluidResults());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, BasinRecipe recipe, IIngredients iingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();

		NonNullList<FluidIngredient> fluidIngredients = recipe.getFluidIngredients();
		List<Pair<Ingredient, MutableInt>> ingredients = ItemHelper.condenseIngredients(recipe.getIngredients());
		List<ItemStack> itemOutputs = recipe.getRollableResultsAsItemStacks();
		NonNullList<FluidStack> fluidOutputs = recipe.getFluidResults();

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

			for (ItemStack itemStack : ingredient.getItems()) {
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

		int outSize = fluidOutputs.size() + recipe.getRollableResults()
			.size();
		int outputIndex = 0;
		
		if (!itemOutputs.isEmpty())
			addStochasticTooltip(itemStacks, recipe.getRollableResults(), i);
		
		for (; outputIndex < outSize; outputIndex++) {
			int xPosition = 141 - (outSize % 2 != 0 && outputIndex == outSize - 1 ? 0 : outputIndex % 2 == 0 ? 10 : -9);
			int yPosition = -19 * (outputIndex / 2) + 50 + yOffset;

			if (itemOutputs.size() > outputIndex) {
				itemStacks.init(i, false, xPosition, yPosition + yOffset);
				itemStacks.set(i, itemOutputs.get(outputIndex));
				i++;
			} else {
				fluidStacks.init(j, false, xPosition + 1, yPosition + 1 + yOffset);
				fluidStacks.set(j, withImprovedVisibility(fluidOutputs.get(outputIndex - itemOutputs.size())));
				j++;
			}
			
		}

		addFluidTooltip(fluidStacks, fluidIngredients, fluidOutputs);

		HeatCondition requiredHeat = recipe.getRequiredHeat();
		if (!requiredHeat.testBlazeBurner(HeatLevel.NONE)) {
			itemStacks.init(i, true, 133, 80);
			itemStacks.set(i, AllBlocks.BLAZE_BURNER.asStack());
			i++;
		}
		if (!requiredHeat.testBlazeBurner(HeatLevel.KINDLED)) {
			itemStacks.init(i, true, 152, 80);
			itemStacks.set(i, AllItems.BLAZE_CAKE.asStack());
			i++;
		}
	}

	@Override
	public void draw(BasinRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
		List<Pair<Ingredient, MutableInt>> actualIngredients = ItemHelper.condenseIngredients(recipe.getIngredients());

		int size = actualIngredients.size() + recipe.getFluidIngredients()
			.size();
		int outSize = recipe.getFluidResults().size() + recipe.getRollableResults().size();
		
		int xOffset = size < 3 ? (3 - size) * 19 / 2 : 0;
		HeatCondition requiredHeat = recipe.getRequiredHeat();
		int yOffset = 0;

		for (int i = 0; i < size; i++)
			AllGuiTextures.JEI_SLOT.draw(matrixStack, 16 + xOffset + (i % 3) * 19, 50 - (i / 3) * 19 + yOffset);

		boolean noHeat = requiredHeat == HeatCondition.NONE;

		int vRows = (1 + outSize) / 2;
		for (int i = 0; i < outSize; i++)
			AllGuiTextures.JEI_SLOT.draw(matrixStack,
				141 - (outSize % 2 != 0 && i == outSize - 1 ? 0 : i % 2 == 0 ? 10 : -9), -19 * (i / 2) + 50 + yOffset);
		if (vRows <= 2)
			AllGuiTextures.JEI_DOWN_ARROW.draw(matrixStack, 136, -19 * (vRows - 1) + 32 + yOffset);

		AllGuiTextures shadow = noHeat ? AllGuiTextures.JEI_SHADOW : AllGuiTextures.JEI_LIGHT;
		shadow.draw(matrixStack, 81, 58 + (noHeat ? 10 : 30));

		if (!needsHeating)
			return;
		
		AllGuiTextures heatBar = noHeat ? AllGuiTextures.JEI_NO_HEAT_BAR : AllGuiTextures.JEI_HEAT_BAR;
		heatBar.draw(matrixStack, 4, 80);
		Minecraft.getInstance().font.draw(matrixStack, Lang.translate(requiredHeat.getTranslationKey()), 9,
			86, requiredHeat.getColor());
	}

}
