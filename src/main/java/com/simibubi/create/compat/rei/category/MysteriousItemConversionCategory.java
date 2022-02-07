package com.simibubi.create.compat.rei.category;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.rei.ConversionRecipe;
import com.simibubi.create.compat.rei.display.CreateDisplay;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.common.util.EntryIngredients;

public class MysteriousItemConversionCategory extends CreateRecipeCategory<ConversionRecipe> {

	public static List<ConversionRecipe> getRecipes() {
		List<ConversionRecipe> recipes = new ArrayList<>();
		recipes.add(ConversionRecipe.create(AllItems.EMPTY_BLAZE_BURNER.asStack(), AllBlocks.BLAZE_BURNER.asStack()));
		recipes.add(ConversionRecipe.create(AllItems.CHROMATIC_COMPOUND.asStack(), AllItems.SHADOW_STEEL.asStack()));
		recipes.add(ConversionRecipe.create(AllItems.CHROMATIC_COMPOUND.asStack(), AllItems.REFINED_RADIANCE.asStack()));
		recipes.add(ConversionRecipe.create(AllBlocks.PECULIAR_BELL.asStack(), AllBlocks.HAUNTED_BELL.asStack()));
		return recipes;
	}

	public MysteriousItemConversionCategory() {
		super(itemIcon(AllItems.CHROMATIC_COMPOUND), emptyBackground(177, 50));
	}

	@Override
	public void addWidgets(CreateDisplay<ConversionRecipe> display, List<Widget> ingredients, Point origin) {
		List<ProcessingOutput> results = display.getRecipe().getRollableResults();
		ingredients.add(basicSlot(point(origin.x + 27, origin.y + 17))
				.markInput()
				.entries(display.getInputEntries().get(0)));
		ingredients.add(basicSlot(point(origin.x + 132, origin.y + 17))
				.markOutput()
				.entries(EntryIngredients.of(results.get(0).getStack())));
	}

	@Override
	public void draw(ConversionRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SLOT.render(matrixStack, 26, 16);
		AllGuiTextures.JEI_SLOT.render(matrixStack, 131, 16);
		AllGuiTextures.JEI_LONG_ARROW.render(matrixStack, 52, 20);
		AllGuiTextures.JEI_QUESTION_MARK.render(matrixStack, 77, 5);
	}

}
