package com.simibubi.create.compat.rei.category;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.rei.category.animations.AnimatedPress;
import com.simibubi.create.compat.rei.display.CreateDisplay;
import com.simibubi.create.content.contraptions.components.press.PressingRecipe;
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

public class PressingCategory extends CreateRecipeCategory<PressingRecipe> {

	public PressingCategory() {
		super(doubleItemIcon(AllBlocks.MECHANICAL_PRESS, AllItems.IRON_SHEET), emptyBackground(177, 80));
	}

	@Override
	public List<Widget> setupDisplay(CreateDisplay<PressingRecipe> display, Rectangle bounds) {
			Point origin = new Point(bounds.getX(), bounds.getY() + 4);
			List<Widget> widgets = new ArrayList<>();
			List<ProcessingOutput> results = display.getRecipe().getRollableResults();
			widgets.add(Widgets.createRecipeBase(bounds));
			widgets.add(Widgets.createSlot(new Point(origin.x + 27, origin.y + 51)).disableBackground().markInput().entries(display.getInputEntries().get(0)));
			widgets.add(WidgetUtil.textured(AllGuiTextures.JEI_SLOT, origin.x + 26, origin.y + 50));
			widgets.add(WidgetUtil.textured(AllGuiTextures.JEI_SHADOW, origin.x + 61, origin.y + 41));
			widgets.add(WidgetUtil.textured(AllGuiTextures.JEI_LONG_ARROW, origin.x + 52, origin.y + 54));

		for (int outputIndex = 0; outputIndex < results.size(); outputIndex++) {
			List<Component> tooltip = new ArrayList<>();
			if (results.get(outputIndex).getChance() != 1)
				tooltip.add(Lang.translate("recipe.processing.chance", results.get(outputIndex).getChance() < 0.01 ? "<1" : (int) (results.get(outputIndex).getChance() * 100))
						.withStyle(ChatFormatting.GOLD));
			widgets.add(Widgets.createSlot(new Point((origin.x + 131 + 19 * outputIndex) + 1, (origin.y + 50) + 1))
					.disableBackground().markOutput()
					.entry(EntryStack.of(VanillaEntryTypes.ITEM, results.get(outputIndex).getStack()).tooltip(tooltip)));
			widgets.add(WidgetUtil.textured(getRenderedSlot(display.getRecipe(), outputIndex), origin.x + 131 + 19 * outputIndex, origin.y + 50));
		}
		AnimatedPress press = new AnimatedPress(false);
		press.setPos(new Point(origin.getX() + (getDisplayWidth(display) / 2 - 17), origin.getY() + 22));
		widgets.add(press);
		return widgets;
	}
}
