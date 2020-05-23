package com.simibubi.create.compat.jei.category;

import java.util.List;
import java.util.Map;

import com.simibubi.create.Create;
import com.simibubi.create.compat.jei.DoubleItemIcon;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.Lang;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public abstract class CreateRecipeCategory<T extends IRecipe<?>> implements IRecipeCategory<T> {

	private ResourceLocation uid;
	private String name;
	private IDrawable icon;
	private IDrawable background;

	public CreateRecipeCategory(String id, IDrawable icon, IDrawable background) {
		uid = new ResourceLocation(Create.ID, id);
		name = id;
		this.background = background;
		this.icon = icon;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public ResourceLocation getUid() {
		return uid;
	}

	@Override
	public String getTitle() {
		return Lang.translate("recipe." + name);
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	protected static AllGuiTextures getRenderedSlot(IRecipe<?> recipe, int index) {
		AllGuiTextures jeiSlot = AllGuiTextures.JEI_SLOT;
		if (!(recipe instanceof ProcessingRecipe))
			return jeiSlot;
		ProcessingRecipe<?> processingRecipe = (ProcessingRecipe<?>) recipe;
		List<ProcessingOutput> rollableResults = processingRecipe.getRollableResults();
		if (rollableResults.size() <= index)
			return jeiSlot;
		if (processingRecipe.getRollableResults().get(index).getChance() == 1)
			return jeiSlot;
		return AllGuiTextures.JEI_CHANCE_SLOT;
	}

	protected static IDrawable emptyBackground(int width, int height) {
		return new EmptyBackground(width, height);
	}

	protected static IDrawable doubleItemIcon(IItemProvider item1, IItemProvider item2) {
		return new DoubleItemIcon(() -> new ItemStack(item1), () -> new ItemStack(item2));
	}

	protected static IDrawable itemIcon(IItemProvider item) {
		return new DoubleItemIcon(() -> new ItemStack(item), () -> ItemStack.EMPTY);
	}

	protected static void addStochasticTooltip(IGuiItemStackGroup itemStacks, List<ProcessingOutput> results) {
		itemStacks.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
			if (input)
				return;
			ProcessingOutput output = results.get(slotIndex - 1);
			if (output.getChance() != 1)
				tooltip.add(1, TextFormatting.GOLD
						+ Lang.translate("recipe.processing.chance", (int) (output.getChance() * 100)));
		});
	}

	protected static void addCatalystTooltip(IGuiItemStackGroup itemStacks, Map<Integer, Float> catalystIndices) {
		itemStacks.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
			if (!input)
				return;
			if (!catalystIndices.containsKey(slotIndex))
				return;
			Float chance = catalystIndices.get(slotIndex);
			tooltip.add(1, TextFormatting.YELLOW + Lang.translate("recipe.processing.catalyst"));
			tooltip.add(2, TextFormatting.GOLD
					+ Lang.translate("recipe.processing.chanceToReturn", (int) (chance.floatValue() * 100)));
		});
	}

}
