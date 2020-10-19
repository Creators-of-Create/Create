package com.simibubi.create.compat.jei.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.simibubi.create.AllFluids;
import com.simibubi.create.Create;
import com.simibubi.create.compat.jei.DoubleItemIcon;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.Lang;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

public abstract class CreateRecipeCategory<T extends IRecipe<?>> implements IRecipeCategory<T> {

	public List<Supplier<? extends Object>> recipeCatalysts = new ArrayList<>();
	public List<Supplier<List<? extends IRecipe<?>>>> recipes = new ArrayList<>();
	public ResourceLocation uid;

	protected String name;
	private IDrawable icon;
	private IDrawable background;

	public CreateRecipeCategory(IDrawable icon, IDrawable background) {
		this.background = background;
		this.icon = icon;
	}

	public void setCategoryId(String name) {
		this.uid = new ResourceLocation(Create.ID, name);
		this.name = name;
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
		if (processingRecipe.getRollableResults()
			.get(index)
			.getChance() == 1)
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
				tooltip.add(1,
					TextFormatting.GOLD + Lang.translate("recipe.processing.chance", (int) (output.getChance() * 100)));
		});
	}

	protected static void addFluidTooltip(IGuiFluidStackGroup fluidStacks, List<FluidIngredient> inputs,
		List<FluidStack> outputs) {
		List<Integer> amounts = new ArrayList<>();
		inputs.forEach(f -> amounts.add(f.getRequiredAmount()));
		outputs.forEach(f -> amounts.add(f.getAmount()));

		fluidStacks.addTooltipCallback((slotIndex, input, fluid, tooltip) -> {
			if (fluid.getFluid()
				.isEquivalentTo(AllFluids.POTION.get())) {
				String name = PotionFluidHandler.getPotionName(fluid)
					.getFormattedText();
				if (tooltip.isEmpty())
					tooltip.add(0, name);
				else
					tooltip.set(0, name);

				ArrayList<ITextComponent> potionTooltip = new ArrayList<>();
				PotionFluidHandler.addPotionTooltip(fluid, potionTooltip, 1);
				tooltip.addAll(1, potionTooltip.stream()
					.map(ITextComponent::getFormattedText)
					.collect(Collectors.toList()));
			}

			int amount = amounts.get(slotIndex);
			String text = TextFormatting.GOLD + Lang.translate("generic.unit.millibuckets", amount);
			if (tooltip.isEmpty())
				tooltip.add(0, text);
			else
				tooltip.set(0, tooltip.get(0) + " " + text);
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
