package com.simibubi.create.compat.jei.category;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.sandPaper.SandPaperItemComponent;
import com.simibubi.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.createmod.catnip.api.client.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class PolishingCategory extends CreateRecipeCategory<SandPaperPolishingRecipe> {

	private final ItemStack renderedSandpaper;

	public PolishingCategory(Info<SandPaperPolishingRecipe> info) {
		super(info);
		renderedSandpaper = AllItems.SAND_PAPER.asStack();
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, SandPaperPolishingRecipe recipe, IFocusGroup focuses) {
		builder
				.addSlot(RecipeIngredientRole.INPUT, 27, 29)
				.setBackground(getRenderedSlot(), -1, -1)
				.addIngredients(recipe.getIngredients().get(0));

		ProcessingOutput output = recipe.getRollableResults().get(0);
		builder
				.addSlot(RecipeIngredientRole.OUTPUT, 132, 29)
				.setBackground(getRenderedSlot(output), -1, -1)
				.addItemStack(output.getStack())
				.addRichTooltipCallback(addStochasticTooltip(output));
	}

	@Override
	public void draw(SandPaperPolishingRecipe recipe, IRecipeSlotsView iRecipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SHADOW.render(graphics, 61, 21);
		AllGuiTextures.JEI_LONG_ARROW.render(graphics, 52, 32);

		List<ItemStack> matchingStacks = getItemStacks(recipe.getIngredients().get(0));
		if (matchingStacks.isEmpty())
			return;

		renderedSandpaper.set(AllDataComponents.SAND_PAPER_POLISHING, new SandPaperItemComponent(matchingStacks.get(0)));
		renderedSandpaper.set(AllDataComponents.SAND_PAPER_JEI, Unit.INSTANCE);
		GuiGameElement.of(renderedSandpaper)
				.<GuiGameElement.GuiRenderBuilder>at(getBackground().getWidth() / 2 - 16, 0, 0)
				.scale(2)
				.render(graphics, 0, 0, 0);
	}

}
