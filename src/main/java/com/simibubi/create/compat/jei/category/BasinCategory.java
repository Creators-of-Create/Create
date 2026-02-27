package com.simibubi.create.compat.jei.category;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.api.recipe.HeatCondition;

import com.simibubi.create.compat.jei.CreateJEI;

import mezz.jei.api.gui.drawable.IDrawable;

import net.minecraft.network.chat.Component;

import org.apache.commons.lang3.mutable.MutableInt;

import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.item.ItemHelper;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.createmod.catnip.data.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

@ParametersAreNonnullByDefault
public class BasinCategory extends CreateRecipeCategory<BasinRecipe> {

	private final boolean needsHeating;

	public BasinCategory(Info<BasinRecipe> info, boolean needsHeating) {
		super(info);
		this.needsHeating = needsHeating;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, BasinRecipe recipe, IFocusGroup focuses) {
		List<Pair<Ingredient, MutableInt>> condensedIngredients = ItemHelper.condenseIngredients(recipe.getIngredients());

		int size = condensedIngredients.size() + recipe.getFluidIngredients().size();
		int xOffset = size < 3 ? (3 - size) * 19 / 2 : 0;
		int i = 0;

		for (Pair<Ingredient, MutableInt> pair : condensedIngredients) {
			List<ItemStack> stacks = new ArrayList<>();
			for (ItemStack itemStack : pair.getFirst().getItems()) {
				ItemStack copy = itemStack.copy();
				copy.setCount(pair.getSecond().getValue());
				stacks.add(copy);
			}

			builder
					.addSlot(RecipeIngredientRole.INPUT, 17 + xOffset + (i % 3) * 19, 51 - (i / 3) * 19)
					.setBackground(getRenderedSlot(), -1, -1)
					.addItemStacks(stacks);
			i++;
		}
		for (SizedFluidIngredient fluidIngredient : recipe.getFluidIngredients()) {
			int x = 17 + xOffset + (i % 3) * 19;
			int y = 51 - (i / 3) * 19;
			addFluidSlot(builder, x, y, fluidIngredient);
			i++;
		}

		size = recipe.getRollableResults().size() + recipe.getFluidResults().size();
		i = 0;

		for (ProcessingOutput result : recipe.getRollableResults()) {
			int xPosition = 142 - (size % 2 != 0 && i == size - 1 ? 0 : i % 2 == 0 ? 10 : -9);
			int yPosition = -19 * (i / 2) + 51;

			builder
					.addSlot(RecipeIngredientRole.OUTPUT, xPosition, yPosition)
					.setBackground(getRenderedSlot(result), -1, -1)
					.addItemStack(result.getStack())
					.addRichTooltipCallback(addStochasticTooltip(result));
			i++;
		}

		for (FluidStack fluidResult : recipe.getFluidResults()) {
			int xPosition = 142 - (size % 2 != 0 && i == size - 1 ? 0 : i % 2 == 0 ? 10 : -9);
			int yPosition = -19 * (i / 2) + 51;
			addFluidSlot(builder, xPosition, yPosition, fluidResult);
			i++;
		}

		HeatCondition requiredHeat = recipe.getRequiredHeat();
		if(requiredHeat == null) return;

		List<ItemStack> itemHints = requiredHeat.getItemHints();
		for(int j = 0; j < itemHints.size(); j++) {
			builder
				.addSlot(RecipeIngredientRole.RENDER_ONLY, 134 + (j * 19), 81)
				.addItemStack(itemHints.get(j));
		}
	}

	@Override
	public void draw(BasinRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		HeatCondition requiredHeat = recipe.getRequiredHeat();

		boolean noHeat = requiredHeat == null;

		int vRows = (1 + recipe.getFluidResults().size() + recipe.getRollableResults().size()) / 2;

		if (vRows <= 2)
			AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 136, -19 * (vRows - 1) + 32);

		AllGuiTextures shadow = noHeat ? AllGuiTextures.JEI_SHADOW : AllGuiTextures.JEI_LIGHT;
		shadow.render(graphics, 81, 58 + (noHeat ? 10 : 30));

		if (!needsHeating)
			return;

		AllGuiTextures heatBar = noHeat ? AllGuiTextures.JEI_NO_HEAT_BAR : AllGuiTextures.JEI_HEAT_BAR;
		heatBar.render(graphics, 4, 80);

		String translationKey = noHeat ? "create.recipe.heat_requirement.create.none" : requiredHeat.getTranslationKey();
		int color = noHeat ? 0xFFFFFF : requiredHeat.getColor();
		graphics.drawString(Minecraft.getInstance().font, Component.translatable(translationKey), 9,
				86, color, false);
	}

	public void drawHeat(BasinRecipe recipe, GuiGraphics graphics, int xOffset, int yOffset) {
		HeatCondition requiredHeat = recipe.getRequiredHeat();
		if (requiredHeat != null) {
			IDrawable drawable = CreateJEI.HEAT_CONDITION_DRAWABLES.get(requiredHeat);
			if (drawable != null)
				drawable.draw(graphics, xOffset, yOffset);
		}
	}
}
