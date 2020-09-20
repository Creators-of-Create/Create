package com.simibubi.create.compat.jei.category;

import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.content.curiosities.tools.SandPaperPolishingRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;

public class PolishingCategory extends CreateRecipeCategory<SandPaperPolishingRecipe> {

	private ItemStack renderedSandpaper;

	public PolishingCategory() {
		super("sandpaper_polishing", itemIcon(AllItems.SAND_PAPER.get()), emptyBackground(177, 55));
		renderedSandpaper = AllItems.SAND_PAPER.asStack();
	}

	@Override
	public Class<? extends SandPaperPolishingRecipe> getRecipeClass() {
		return SandPaperPolishingRecipe.class;
	}

	@Override
	public void setIngredients(SandPaperPolishingRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutputs(VanillaTypes.ITEM, recipe.getRollableResultsAsItemStacks());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, SandPaperPolishingRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		List<ProcessingOutput> results = recipe.getRollableResults();

		itemStacks.init(0, true, 26, 28);
		itemStacks.set(0, Arrays.asList(recipe.getIngredients().get(0).getMatchingStacks()));
		itemStacks.init(1, false, 131, 28);
		itemStacks.set(1, results.get(0).getStack());

		addStochasticTooltip(itemStacks, results);
	}

	@Override
	public void draw(SandPaperPolishingRecipe recipe, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SLOT.draw(26, 28);
		getRenderedSlot(recipe, 0).draw(131, 28);
		AllGuiTextures.JEI_SHADOW.draw(61, 21);
		AllGuiTextures.JEI_LONG_ARROW.draw(52, 32);

		NonNullList<Ingredient> ingredients = recipe.getIngredients();
		ItemStack[] matchingStacks = ingredients.get(0).getMatchingStacks();
		if (matchingStacks.length == 0)
			return;

		RenderSystem.pushMatrix();
		CompoundNBT tag = renderedSandpaper.getOrCreateTag();
		tag.put("Polishing", matchingStacks[0].serializeNBT());
		tag.putBoolean("JEI", true);
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		RenderSystem.scaled(2, 2, 2);
		itemRenderer.renderItemIntoGUI(renderedSandpaper, getBackground().getWidth() / 4 - 8, 1);
		RenderSystem.popMatrix();
	}

}
