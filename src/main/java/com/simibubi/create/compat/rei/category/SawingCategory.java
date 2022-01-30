package com.simibubi.create.compat.rei.category;

import java.util.ArrayList;
import java.util.List;

//import java.util.Arrays;
//import java.util.List;

//import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.rei.category.animations.AnimatedSaw;
import com.simibubi.create.compat.rei.display.CreateDisplay;
import com.simibubi.create.content.contraptions.components.saw.CuttingRecipe;
//import mezz.jei.api.constants.VanillaTypes;
//import mezz.jei.api.gui.IRecipeLayout;
//import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
//import mezz.jei.api.ingredients.IIngredients;
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
import net.minecraft.world.item.Items;

public class SawingCategory extends CreateRecipeCategory<CuttingRecipe> {

	public SawingCategory() {
		super(doubleItemIcon(AllBlocks.MECHANICAL_SAW, () -> Items.OAK_LOG), emptyBackground(177, 70 + 10));
	}


//	@Override
//	public void setIngredients(CuttingRecipe recipe, IIngredients ingredients) {
//		ingredients.setInputIngredients(recipe.getIngredients());
//		ingredients.setOutputs(VanillaTypes.ITEM, recipe.getRollableResultsAsItemStacks());
//	}
//
//	@Override
//	public void setRecipe(IRecipeLayout recipeLayout, CuttingRecipe recipe, IIngredients ingredients) {
//		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
//		itemStacks.init(0, true, 43, 4);
//		itemStacks.set(0, Arrays.asList(recipe.getIngredients()
//			.get(0)
//			.getItems()));
//
//		List<ProcessingOutput> results = recipe.getRollableResults();
//		for (int outputIndex = 0; outputIndex < results.size(); outputIndex++) {
//			int xOffset = outputIndex % 2 == 0 ? 0 : 19;
//			int yOffset = (outputIndex / 2) * -19;
//
//			itemStacks.init(outputIndex + 1, false, 117 + xOffset, 47 + yOffset);
//			itemStacks.set(outputIndex + 1, results.get(outputIndex)
//				.getStack());
//		}
//
//		addStochasticTooltip(itemStacks, results);
//	}
//
//	@Override
//	public void draw(CuttingRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
//		AllGuiTextures.JEI_SLOT.render(matrixStack, 43, 4);
//		int size = recipe.getRollableResults()
//			.size();
//		for (int i = 0; i < size; i++) {
//			int xOffset = i % 2 == 0 ? 0 : 19;
//			int yOffset = (i / 2) * -19;
//			getRenderedSlot(recipe, i).render(matrixStack, 117 + xOffset, 47 + yOffset);
//		}
//		AllGuiTextures.JEI_DOWN_ARROW.render(matrixStack, 70, 6);
//		AllGuiTextures.JEI_SHADOW.render(matrixStack, 72 - 17, 42 + 13);
//		saw.draw(matrixStack, 72, 42);
//	}

	@Override
	public List<Widget> setupDisplay(CreateDisplay<CuttingRecipe> display, Rectangle bounds) {
		Point origin = new Point(bounds.getX(), bounds.getY() + 4);
		List<Widget> widgets = new ArrayList<>();
		List<ProcessingOutput> results = display.getRecipe().getRollableResults();
		widgets.add(Widgets.createRecipeBase(bounds));
		widgets.add(Widgets.createSlot(new Point(origin.x + 44, origin.y + 5)).disableBackground().markInput().entries(display.getInputEntries().get(0)));
		widgets.add(WidgetUtil.textured(AllGuiTextures.JEI_SLOT, origin.x + 43, origin.y + 4));
		widgets.add(WidgetUtil.textured(AllGuiTextures.JEI_DOWN_ARROW, origin.x + 70, origin.y + 6));
		widgets.add(WidgetUtil.textured(AllGuiTextures.JEI_SHADOW, origin.x + 72 - 17, origin.y + 42 + 13));

		for (int outputIndex = 0; outputIndex < results.size(); outputIndex++) {
			int xOffset = outputIndex % 2 == 0 ? 0 : 19;
			int yOffset = (outputIndex / 2) * -19;
			List<Component> tooltip = new ArrayList<>();
			if (results.get(outputIndex).getChance() != 1)
				tooltip.add(Lang.translate("recipe.processing.chance", results.get(outputIndex).getChance() < 0.01 ? "<1" : (int) (results.get(outputIndex).getChance() * 100))
						.withStyle(ChatFormatting.GOLD));
			widgets.add(Widgets.createSlot(new Point((origin.x + 117 + xOffset) + 1, (origin.y + 47 + yOffset) + 1)).disableBackground().markOutput().entry(EntryStack.of(VanillaEntryTypes.ITEM, results.get(outputIndex).getStack()).tooltip(tooltip)));
			widgets.add(WidgetUtil.textured(getRenderedSlot(display.getRecipe(), outputIndex), origin.x + 117 + xOffset, origin.y + 47 + yOffset));
		}

		AnimatedSaw saw = new AnimatedSaw();
		saw.setPos(new Point(origin.x + 72, origin.y + 42));
		widgets.add(saw);

		return widgets;
	}
}
