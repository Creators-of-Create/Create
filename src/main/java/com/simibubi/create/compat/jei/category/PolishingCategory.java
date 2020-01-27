package com.simibubi.create.compat.jei.category;

import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.compat.jei.CreateJEI;
import com.simibubi.create.compat.jei.DoubleItemIcon;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.processing.ProcessingOutput;
import com.simibubi.create.modules.curiosities.tools.SandPaperPolishingRecipe;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public class PolishingCategory implements IRecipeCategory<SandPaperPolishingRecipe> {

	private static ResourceLocation ID = new ResourceLocation(Create.ID, "sandpaper_polishing");
	private IDrawable icon;
	private IDrawable background = new EmptyBackground(177, 55);
	private ItemStack renderedSandpaper;

	public PolishingCategory() {
		icon = new DoubleItemIcon(() -> AllItems.SAND_PAPER.asStack(), () -> ItemStack.EMPTY);
		renderedSandpaper = AllItems.SAND_PAPER.asStack();
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
	public Class<? extends SandPaperPolishingRecipe> getRecipeClass() {
		return SandPaperPolishingRecipe.class;
	}

	@Override
	public String getTitle() {
		return Lang.translate("recipe.sandpaper_polishing");
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void setIngredients(SandPaperPolishingRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutputs(VanillaTypes.ITEM, recipe.getPossibleOutputs());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, SandPaperPolishingRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		List<ProcessingOutput> results = recipe.getRollableResults();

		itemStacks.init(0, true, 26, 28);
		itemStacks.set(0, Arrays.asList(recipe.getIngredients().get(0).getMatchingStacks()));
		itemStacks.init(1, false, 131, 28);
		itemStacks.set(1, results.get(0).getStack());

		CreateJEI.addStochasticTooltip(itemStacks, results);
	}

	@Override
	public void draw(SandPaperPolishingRecipe recipe, double mouseX, double mouseY) {
		ScreenResources.JEI_SLOT.draw(26, 28);
		ScreenResources.JEI_SLOT.draw(131, 28);
		ScreenResources.JEI_SHADOW.draw(61, 21);
		ScreenResources.JEI_LONG_ARROW.draw(52, 32);

		NonNullList<Ingredient> ingredients = recipe.getIngredients();
		ItemStack[] matchingStacks = ingredients.get(0).getMatchingStacks();
		if (matchingStacks.length == 0)
			return;

		GlStateManager.pushMatrix();
		CompoundNBT tag = renderedSandpaper.getOrCreateTag();
		tag.put("Polishing", matchingStacks[0].serializeNBT());
		tag.putBoolean("JEI", true);
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		GlStateManager.scaled(2, 2, 2);
		itemRenderer.renderItemIntoGUI(renderedSandpaper, getBackground().getWidth() / 4 - 8, 1);
		GlStateManager.popMatrix();
	}

}
