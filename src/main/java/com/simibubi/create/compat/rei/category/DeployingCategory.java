package com.simibubi.create.compat.rei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.rei.category.animations.AnimatedDeployer;
import com.simibubi.create.compat.rei.display.CreateDisplay;
import com.simibubi.create.content.contraptions.components.deployer.DeployerApplicationRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import com.simibubi.create.foundation.utility.Lang;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.util.ClientEntryStacks;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.ChatFormatting;

import java.util.List;

public class DeployingCategory extends CreateRecipeCategory<DeployerApplicationRecipe> {

	AnimatedDeployer deployer;

	public DeployingCategory() {
		super(itemIcon(AllBlocks.DEPLOYER), emptyBackground(177, 80));
		deployer = new AnimatedDeployer();
	}

	@Override
	public void addWidgets(CreateDisplay<DeployerApplicationRecipe> display, List<Widget> ingredients, Point origin) {
		DeployerApplicationRecipe recipe = display.getRecipe();
		Slot input = basicSlot(origin.getX() + 27, origin.getY() + 51)
				.markInput()
				.entries(EntryIngredients.ofIngredient(recipe.getProcessedItem()));
		ClientEntryStacks.setTooltipProcessor(input.getCurrentEntry(), ((entryStack, tooltip) -> {
			if (recipe.shouldKeepHeldItem())
					tooltip.add(Lang.translate("recipe.deploying.not_consumed")
							.withStyle(ChatFormatting.GOLD));
			return tooltip;
		}));
		ingredients.add(input);
		ingredients.add(basicSlot(new Point(origin.getX() + 51, origin.getY() + 5))
				.markInput()
				.entries(EntryIngredients.ofIngredient(recipe.getRequiredHeldItem())));
		Slot output = basicSlot(origin.getX() + 132, origin.getY() + 51)
				.markOutput()
				.entries(EntryIngredients.of(recipe.getResultItem()));
		ingredients.add(output);
		addStochasticTooltip(ingredients, recipe.getRollableResults(), 2);
	}

	@Override
	public void draw(DeployerApplicationRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SLOT.render(matrixStack, 50, 4);
		AllGuiTextures.JEI_SLOT.render(matrixStack, 26, 50);
		getRenderedSlot(recipe, 0).render(matrixStack, 131, 50);
		AllGuiTextures.JEI_SHADOW.render(matrixStack, 62, 57);
		AllGuiTextures.JEI_DOWN_ARROW.render(matrixStack, 126, 29);
		deployer.draw(matrixStack, getDisplayWidth(null) / 2 - 13, 22);
	}

}
