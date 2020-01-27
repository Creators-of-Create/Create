package com.simibubi.create.compat.jei.category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.compat.jei.ConversionRecipe;
import com.simibubi.create.compat.jei.CreateJEI;
import com.simibubi.create.compat.jei.DoubleItemIcon;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.processing.ProcessingOutput;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class MysteriousItemConversionCategory implements IRecipeCategory<ConversionRecipe> {

	private static ResourceLocation ID = new ResourceLocation(Create.ID, "mystery_conversion");
	private IDrawable icon;
	private IDrawable background = new EmptyBackground(177, 50);

	public static List<ConversionRecipe> getRecipes() {
		List<ConversionRecipe> recipes = new ArrayList<>();
		recipes.add(ConversionRecipe.create(AllItems.CHROMATIC_COMPOUND.asStack(), AllItems.SHADOW_STEEL.asStack()));
		recipes.add(ConversionRecipe.create(AllItems.CHROMATIC_COMPOUND.asStack(), AllItems.REFINED_RADIANCE.asStack()));
		return recipes;
	}
	
	public MysteriousItemConversionCategory() {
		icon = new DoubleItemIcon(() -> AllItems.CHROMATIC_COMPOUND.asStack(), () -> ItemStack.EMPTY);
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public ResourceLocation getUid() {
		return ID;
	}

	@Override
	public Class<? extends ConversionRecipe> getRecipeClass() {
		return ConversionRecipe.class;
	}

	@Override
	public String getTitle() {
		return Lang.translate("recipe.mystery_conversion");
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void setIngredients(ConversionRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutputs(VanillaTypes.ITEM, recipe.getPossibleOutputs());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, ConversionRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		List<ProcessingOutput> results = recipe.getRollableResults();

		itemStacks.init(0, true, 26, 16);
		itemStacks.set(0, Arrays.asList(recipe.getIngredients().get(0).getMatchingStacks()));
		itemStacks.init(1, false, 131, 16);
		itemStacks.set(1, results.get(0).getStack());

		CreateJEI.addStochasticTooltip(itemStacks, results);
	}

	@Override
	public void draw(ConversionRecipe recipe, double mouseX, double mouseY) {
		ScreenResources.JEI_SLOT.draw(26, 16);
		ScreenResources.JEI_SLOT.draw(131, 16);
		ScreenResources.JEI_LONG_ARROW.draw(52, 20);
		ScreenResources.JEI_QUESTION_MARK.draw(77, 5);
	}

}
