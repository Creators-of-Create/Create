package com.simibubi.create.compat.jei;

import java.util.Arrays;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.components.crusher.CrushingRecipe;
import com.simibubi.create.modules.contraptions.processing.StochasticOutput;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public class CrushingCategory implements IRecipeCategory<CrushingRecipe> {

	private static ResourceLocation ID = new ResourceLocation(Create.ID, "crushing");
	private AnimatedCrushingWheels crushingWheels = new AnimatedCrushingWheels();
	private IDrawable icon;
	private IDrawable background = new EmptyBackground(177, 100);

	public CrushingCategory() {
		icon = new DoubleItemIcon(() -> new ItemStack(AllBlocks.CRUSHING_WHEEL.get()),
				() -> new ItemStack(AllItems.FLOUR.get()));
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public ResourceLocation getUid() {
		return ID;
	}

	@Override
	public Class<? extends CrushingRecipe> getRecipeClass() {
		return CrushingRecipe.class;
	}

	@Override
	public String getTitle() {
		return Lang.translate("recipe.crushing");
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setIngredients(CrushingRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutputs(VanillaTypes.ITEM, recipe.getPossibleOutputs());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, CrushingRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		itemStacks.init(0, true, 50, 2);
		itemStacks.set(0, Arrays.asList(recipe.getIngredients().get(0).getMatchingStacks()));

		List<StochasticOutput> results = recipe.getRollableResults();
		int size = results.size();
		int offset = -size * 19 / 2;
		for (int outputIndex = 0; outputIndex < size; outputIndex++) {
			itemStacks.init(outputIndex + 1, false, getBackground().getWidth() / 2 + offset + 19 * outputIndex, 78);
			itemStacks.set(outputIndex + 1, results.get(outputIndex).getStack());
		}

		itemStacks.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
			if (input)
				return;
			StochasticOutput output = results.get(slotIndex - 1);
			if (output.getChance() != 1)
				tooltip.add(1, TextFormatting.GOLD
						+ Lang.translate("recipe.processing.chance", (int) (output.getChance() * 100)));
		});
	}

	@Override
	public void draw(CrushingRecipe recipe, double mouseX, double mouseY) {
		List<StochasticOutput> results = recipe.getRollableResults();
		ScreenResources.JEI_SLOT.draw(50, 2);
		ScreenResources.JEI_DOWN_ARROW.draw(72, 7);

		int size = results.size();
		int offset = -size * 19 / 2;
		for (int outputIndex = 0; outputIndex < results.size(); outputIndex++)
			ScreenResources.JEI_SLOT.draw(getBackground().getWidth() / 2 + offset + 19 * outputIndex, 78);

		crushingWheels.draw(92, 49);
	}

}
