package com.simibubi.create.compat.jei.category;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.jei.category.animations.AnimatedPress;
import com.simibubi.create.compat.jei.display.PressingDisplay;
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

public class PressingCategory extends CreateRecipeCategory<PressingRecipe, PressingDisplay> {

	public PressingCategory() {
		super(doubleItemIcon(AllBlocks.MECHANICAL_PRESS, AllItems.IRON_SHEET), emptyBackground(177, 80));
	}
//
//	private AnimatedPress press = new AnimatedPress(false);
//
//	public PressingCategory() {
//		super(doubleItemIcon(AllBlocks.MECHANICAL_PRESS.get(), AllItems.IRON_SHEET.get()), emptyBackground(177, 70));
//	}
//
//	@Override
//	public Class<? extends PressingRecipe> getRecipeClass() {
//		return PressingRecipe.class;
//	}
//
//	@Override
//	public void setIngredients(PressingRecipe recipe, IIngredients ingredients) {
//		ingredients.setInputIngredients(recipe.getIngredients());
//		ingredients.setOutputs(VanillaTypes.ITEM, recipe.getRollableResultsAsItemStacks());
//	}
//
//	@Override
//	public void setRecipe(IRecipeLayout recipeLayout, PressingRecipe recipe, IIngredients ingredients) {
//		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
//		itemStacks.init(0, true, 26, 50);
//		itemStacks.set(0, Arrays.asList(recipe.getIngredients()
//			.get(0)
//			.getItems()));
//
//		List<ProcessingOutput> results = recipe.getRollableResults();
//		for (int outputIndex = 0; outputIndex < results.size(); outputIndex++) {
//			itemStacks.init(outputIndex + 1, false, 131 + 19 * outputIndex, 50);
//			itemStacks.set(outputIndex + 1, results.get(outputIndex)
//				.getStack());
//		}
//
//		addStochasticTooltip(itemStacks, results);
//	}
//
//	@Override
//	public void draw(PressingRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
//		AllGuiTextures.JEI_SLOT.render(matrixStack, 26, 50);
//		getRenderedSlot(recipe, 0).render(matrixStack, 131, 50);
//		if (recipe.getRollableResults()
//			.size() > 1)
//			getRenderedSlot(recipe, 1).render(matrixStack, 131 + 19, 50);
//		AllGuiTextures.JEI_SHADOW.render(matrixStack, 61, 41);
//		AllGuiTextures.JEI_LONG_ARROW.render(matrixStack, 52, 54);
//		press.draw(matrixStack, getBackground().getWidth() / 2 - 17, 22);
//	}
//

	@Override
	public List<Widget> setupDisplay(PressingDisplay display, Rectangle bounds) {
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
