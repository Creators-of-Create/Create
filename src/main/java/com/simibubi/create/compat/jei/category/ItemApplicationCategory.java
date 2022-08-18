package com.simibubi.create.compat.jei.category;

import java.util.Optional;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.content.contraptions.processing.ItemApplicationRecipe;
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

		builder.addSlot(RecipeIngredientRole.OUTPUT, 132, 38)
				.setBackground(getRenderedSlot(recipe.getRollableResults().get(0)), -1, -1)
				.addItemStack(recipe.getResultItem())
				.addTooltipCallback(addStochasticTooltip(recipe.getRollableResults().get(0)));
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
