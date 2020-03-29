package com.simibubi.create.compat.jei.category;

import java.util.Arrays;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.compat.jei.CreateJEI;
import com.simibubi.create.compat.jei.DoubleItemIcon;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.compat.jei.category.animations.AnimatedMillstone;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.components.crusher.AbstractCrushingRecipe;
import com.simibubi.create.modules.contraptions.processing.ProcessingOutput;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class MillingCategory implements IRecipeCategory<AbstractCrushingRecipe> {

	private static ResourceLocation ID = new ResourceLocation(Create.ID, "milling");
	private AnimatedMillstone millstone = new AnimatedMillstone();
	private IDrawable icon;
	private IDrawable background = new EmptyBackground(177, 53);

	public MillingCategory() {
		icon = new DoubleItemIcon(() -> new ItemStack(AllBlocks.MILLSTONE.get()),
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
	public Class<? extends AbstractCrushingRecipe> getRecipeClass() {
		return AbstractCrushingRecipe.class;
	}

	@Override
	public String getTitle() {
		return Lang.translate("recipe.milling");
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setIngredients(AbstractCrushingRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutputs(VanillaTypes.ITEM, recipe.getPossibleOutputs());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, AbstractCrushingRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		itemStacks.init(0, true, 14, 8);
		itemStacks.set(0, Arrays.asList(recipe.getIngredients().get(0).getMatchingStacks()));

		List<ProcessingOutput> results = recipe.getRollableResults();
		boolean single = results.size() == 1;
		for (int outputIndex = 0; outputIndex < results.size(); outputIndex++) {
			int xOffset = outputIndex % 2 == 0 ? 0 : 19;
			int yOffset = (outputIndex / 2) * -19;

			itemStacks.init(outputIndex + 1, false, single ? 139 : 133 + xOffset, 27 + yOffset);
			itemStacks.set(outputIndex + 1, results.get(outputIndex).getStack());
		}

		CreateJEI.addStochasticTooltip(itemStacks, results);
	}

	@Override
	public void draw(AbstractCrushingRecipe recipe, double mouseX, double mouseY) {
		int size = recipe.getPossibleOutputs().size();

		ScreenResources.JEI_SLOT.draw(14, 8);
		ScreenResources.JEI_SHADOW.draw(30, 40);
		ScreenResources.JEI_ARROW.draw(85, 32);
		ScreenResources.JEI_DOWN_ARROW.draw(43, 4);

		if (size > 1) {
			for (int i = 0; i < size; i++) {
				int xOffset = i % 2 == 0 ? 0 : 19;
				int yOffset = (i / 2) * -19;
				ScreenResources.JEI_SLOT.draw(133 + xOffset, 27 + yOffset);
			}
		} else {
			ScreenResources.JEI_SLOT.draw(139, 27);
		}

		millstone.draw(57, 27);
	}

}
