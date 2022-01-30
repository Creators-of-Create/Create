package com.simibubi.create.compat.rei.category;

import java.util.ArrayList;
import java.util.List;

//import java.util.Arrays;

//import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.rei.category.animations.AnimatedMillstone;
import com.simibubi.create.compat.rei.display.CreateDisplay;
import com.simibubi.create.content.contraptions.components.millstone.MillingRecipe;
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

public class MillingCategory extends CreateRecipeCategory<MillingRecipe> {

//	private AnimatedMillstone millstone = new AnimatedMillstone();

	public MillingCategory() {
		super(doubleItemIcon(AllBlocks.MILLSTONE, AllItems.WHEAT_FLOUR), emptyBackground(177, 53 + 8));
	}

	@Override
	public List<Widget> setupDisplay(CreateDisplay<MillingRecipe> display, Rectangle bounds) {
		Point origin = new Point(bounds.getX(), bounds.getY() + 4);
		List<Widget> widgets = new ArrayList<>();
		List<ProcessingOutput> results = display.getRecipe().getRollableResults();
		widgets.add(Widgets.createRecipeBase(bounds));
		widgets.add(Widgets.createSlot(new Point(origin.x + 15, origin.y + 9)).disableBackground().markInput().entries(display.getInputEntries().get(0)));
		widgets.add(WidgetUtil.textured(AllGuiTextures.JEI_SLOT, origin.x + 14, origin.y + 8));
		widgets.add(WidgetUtil.textured(AllGuiTextures.JEI_DOWN_ARROW, origin.x + 43, origin.y + 4));
		widgets.add(WidgetUtil.textured(AllGuiTextures.JEI_ARROW, origin.x + 85, origin.y + 32));

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
