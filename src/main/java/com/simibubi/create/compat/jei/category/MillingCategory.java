package com.simibubi.create.compat.jei.category;

import java.util.ArrayList;
import java.util.List;

//import java.util.Arrays;

//import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.jei.category.animations.AnimatedMillstone;
import com.simibubi.create.compat.jei.display.MillingDisplay;
import com.simibubi.create.content.contraptions.components.crusher.AbstractCrushingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.Lang;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

//import mezz.jei.api.constants.VanillaTypes;
//import mezz.jei.api.gui.IRecipeLayout;
//import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
//import mezz.jei.api.ingredients.IIngredients;

public class MillingCategory extends CreateRecipeCategory<AbstractCrushingRecipe, MillingDisplay> {

//	private AnimatedMillstone millstone = new AnimatedMillstone();

	public MillingCategory() {
		super(doubleItemIcon(AllBlocks.MILLSTONE, AllItems.WHEAT_FLOUR), emptyBackground(177, 53 + 10));
	}

//	@Override
//	public Class<? extends AbstractCrushingRecipe> getRecipeClass() {
//		return AbstractCrushingRecipe.class;
//	}
//
//	@Override
//	public void setIngredients(AbstractCrushingRecipe recipe, IIngredients ingredients) {
//		ingredients.setInputIngredients(recipe.getIngredients());
//		ingredients.setOutputs(VanillaTypes.ITEM, recipe.getRollableResultsAsItemStacks());
//	}
//
//	@Override
//	public void setRecipe(IRecipeLayout recipeLayout, AbstractCrushingRecipe recipe, IIngredients ingredients) {
//		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
//		itemStacks.init(0, true, 14, 8);
//		itemStacks.set(0, Arrays.asList(recipe.getIngredients()
//			.get(0)
//			.getItems()));
//
//		List<ProcessingOutput> results = recipe.getRollableResults();
//		boolean single = results.size() == 1;
//		for (int outputIndex = 0; outputIndex < results.size(); outputIndex++) {
//			int xOffset = outputIndex % 2 == 0 ? 0 : 19;
//			int yOffset = (outputIndex / 2) * -19;
//
//			itemStacks.init(outputIndex + 1, false, single ? 139 : 133 + xOffset, 27 + yOffset);
//			itemStacks.set(outputIndex + 1, results.get(outputIndex)
//				.getStack());
//		}
//
//		addStochasticTooltip(itemStacks, results);
//	}
//
//	@Override
//	public void draw(AbstractCrushingRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
//		int size = recipe.getRollableResultsAsItemStacks()
//			.size();
//
//		AllGuiTextures.JEI_SLOT.render(matrixStack, 14, 8);
//		AllGuiTextures.JEI_ARROW.render(matrixStack, 85, 32);
//		AllGuiTextures.JEI_DOWN_ARROW.render(matrixStack, 43, 4);
//		millstone.draw(matrixStack, 48, 27);
//
//		if (size == 1) {
//			getRenderedSlot(recipe, 0).render(matrixStack, 139, 27);
//			return;
//		}
//
//		for (int i = 0; i < size; i++) {
//			int xOffset = i % 2 == 0 ? 0 : 19;
//			int yOffset = (i / 2) * -19;
//			getRenderedSlot(recipe, i).render(matrixStack, 133 + xOffset, 27 + yOffset);
//		}
//
//	}

	@Override
	public List<Widget> setupDisplay(MillingDisplay display, Rectangle bounds) {
		Point origin = new Point(bounds.getX(), bounds.getY() + 4);
		List<Widget> widgets = new ArrayList<>();
		List<ProcessingOutput> results = display.getRecipe().getRollableResults();
		widgets.add(Widgets.createRecipeBase(bounds));
		widgets.add(Widgets.createSlot(new Point(origin.x + 15, origin.y + 9)).disableBackground().markInput().entries(display.getInputEntries().get(0)));
		widgets.add(WidgetUtil.textured(AllGuiTextures.JEI_SLOT, origin.x + 14, origin.y + 8));
		widgets.add(WidgetUtil.textured(AllGuiTextures.JEI_DOWN_ARROW, origin.x + 43, origin.y + 4));
		widgets.add(WidgetUtil.textured(AllGuiTextures.JEI_ARROW, origin.x + 85, origin.y + 32));

		int size = results.size();
		for (int outputIndex = 0; outputIndex < results.size(); outputIndex++) {
			int xOffset = outputIndex % 2 == 0 ? 0 : 19;
			int yOffset = (outputIndex / 2) * -19;
			List<Component> tooltip = new ArrayList<>();
			if (results.get(outputIndex).getChance() != 1)
				tooltip.add(Lang.translate("recipe.processing.chance", results.get(outputIndex).getChance() < 0.01 ? "<1" : (int) (results.get(outputIndex).getChance() * 100))
						.withStyle(ChatFormatting.GOLD));
			widgets.add(Widgets.createSlot(new Point((origin.x + 133 + xOffset) + 1, (origin.y + 27 + yOffset) + 1)).disableBackground().markOutput().entry(EntryStack.of(VanillaEntryTypes.ITEM, results.get(outputIndex).getStack()).tooltip(tooltip)));
			widgets.add(WidgetUtil.textured(getRenderedSlot(display.getRecipe(), outputIndex), origin.x + 133 + xOffset, origin.y + 27 + yOffset));
		}

		AnimatedMillstone millstone = new AnimatedMillstone();
		millstone.setPos(new Point(origin.x + 48, origin.y + 27));
		widgets.add(millstone);

		return widgets;
	}
}
