package com.simibubi.create.compat.rei.category;

import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.rei.category.animations.AnimatedKinetics;
import com.simibubi.create.compat.rei.display.CreateDisplay;
import com.simibubi.create.content.contraptions.components.fan.HauntingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.element.GuiGameElement;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class FanHauntingCategory extends ProcessingViaFanCategory<HauntingRecipe, CreateDisplay<HauntingRecipe>> {

	public FanHauntingCategory() {
		super(185, doubleItemIcon(AllItems.PROPELLER, () -> Items.SOUL_CAMPFIRE));
	}

	public static CreateDisplay<HauntingRecipe> display(HauntingRecipe recipe) {
		return new CreateDisplay<>(recipe, "fan_haunting");
	}

	@Override
	public void addWidgets(CreateDisplay<HauntingRecipe> display, List<Widget> ingredients, Point origin) {
		List<ProcessingOutput> results = display.getRecipe().getRollableResults();
		int xOffsetGlobal = 8 * (3 - Math.min(3, results.size()));

		ingredients.add(basicSlot(origin.x + xOffsetGlobal + 13, origin.y + 48).markInput().entries(EntryIngredients.ofItemStacks(Arrays.asList(display.getRecipe().getIngredients()
				.get(0)
				.getItems()))));

		boolean single = results.size() == 1;
		boolean excessive = results.size() > 9;
		for (int outputIndex = 0; outputIndex < results.size(); outputIndex++) {
			int xOffset = (outputIndex % 3) * 19;
			int yOffset = (outputIndex / 3) * -19;

			ingredients.add(basicSlot(origin.x + xOffsetGlobal + (single ? 126 : 126 + xOffset) + 1,
					origin.y + 48 + yOffset + (excessive ? 8 : 0))
					.markOutput()
					.entries(EntryIngredients.of(results.get(outputIndex)
							.getStack())));
		}

		addStochasticTooltip(ingredients, results);
	}

	@Override
	protected void renderWidgets(PoseStack matrixStack, HauntingRecipe recipe, double mouseX, double mouseY) {
		int size = recipe.getRollableResultsAsItemStacks()
			.size();
		int xOffsetGlobal = 8 * (3 - Math.min(3, size));

		AllGuiTextures.JEI_SLOT.render(matrixStack, xOffsetGlobal + 12, 47);
		AllGuiTextures.JEI_SHADOW.render(matrixStack, 47 + 4, 29);
		AllGuiTextures.JEI_SHADOW.render(matrixStack, 66 + 4, 39);
		AllGuiTextures.JEI_LONG_ARROW.render(matrixStack, xOffsetGlobal + 42, 51);

		if (size == 1) {
			getRenderedSlot(recipe, 0).render(matrixStack, xOffsetGlobal + 126, 47);
			return;
		}

		for (int i = 0; i < size; i++) {
			int xOffset = (i % 3) * 19;
			int yOffset = (i / 3) * -19 + (size > 9 ? 8 : 0);
			getRenderedSlot(recipe, i).render(matrixStack, xOffsetGlobal + 126 + xOffset, 47 + yOffset);
		}
	}

	@Override
	protected void translateFan(PoseStack ms) {
		ms.translate(56 + 4, 33, 0);
	}

	@Override
	public void renderAttachedBlock(PoseStack matrixStack) {
		matrixStack.pushPose();

		GuiGameElement.of(Blocks.SOUL_FIRE.defaultBlockState())
			.scale(24)
			.atLocal(0, 0, 2)
			.lighting(AnimatedKinetics.DEFAULT_LIGHTING)
			.render(matrixStack);

		matrixStack.popPose();
	}

}
