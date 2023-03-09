package com.simibubi.create.compat.jei.category;

import java.util.List;
import java.util.Optional;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.content.contraptions.processing.ItemApplicationRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.utility.Lang;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@ParametersAreNonnullByDefault
public class ItemApplicationCategory extends CreateRecipeCategory<ItemApplicationRecipe> {

	public ItemApplicationCategory(Info<ItemApplicationRecipe> info) {
		super(info);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ItemApplicationRecipe recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, 27, 38)
				.setBackground(getRenderedSlot(), -1, -1)
				.addIngredients(recipe.getProcessedItem());

		builder.addSlot(RecipeIngredientRole.INPUT, 51, 5)
				.setBackground(getRenderedSlot(), -1, -1)
				.addIngredients(recipe.getRequiredHeldItem())
				.addTooltipCallback(
					recipe.shouldKeepHeldItem()
						? (view, tooltip) -> tooltip.add(1, Lang.translateDirect("recipe.deploying.not_consumed")
							.withStyle(ChatFormatting.GOLD))
						: (view, tooltip) -> {}
				);

		List<ProcessingOutput> results = recipe.getRollableResults();
		boolean single = results.size() == 1;
		for (int i = 0; i < results.size(); i++) {
			ProcessingOutput output = results.get(i);
			int xOffset = i % 2 == 0 ? 0 : 19;
			int yOffset = (i / 2) * -19;
			builder.addSlot(RecipeIngredientRole.OUTPUT, single ? 132 : 132 + xOffset, 38 + yOffset)
				.setBackground(getRenderedSlot(output), -1, -1)
				.addItemStack(output.getStack())
				.addTooltipCallback(addStochasticTooltip(output));
		}
	}

	@Override
	public void draw(ItemApplicationRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack matrixStack, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SHADOW.render(matrixStack, 62, 47);
		AllGuiTextures.JEI_DOWN_ARROW.render(matrixStack, 74, 10);

		Optional<ItemStack> displayedIngredient = recipeSlotsView.getSlotViews()
			.get(0)
			.getDisplayedIngredient(VanillaTypes.ITEM_STACK);
		if (displayedIngredient.isEmpty())
			return;

		Item item = displayedIngredient.get()
			.getItem();
		if (!(item instanceof BlockItem blockItem))
			return;

		BlockState state = blockItem.getBlock()
			.defaultBlockState();

		matrixStack.pushPose();
		matrixStack.translate(74, 51, 100);
		matrixStack.mulPose(Vector3f.XP.rotationDegrees(-15.5f));
		matrixStack.mulPose(Vector3f.YP.rotationDegrees(22.5f));
		int scale = 20;

		GuiGameElement.of(state)
			.lighting(AnimatedKinetics.DEFAULT_LIGHTING)
			.scale(scale)
			.render(matrixStack);

		matrixStack.popPose();
	}

}
