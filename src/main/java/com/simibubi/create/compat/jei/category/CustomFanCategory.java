package com.simibubi.create.compat.jei.category;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.content.contraptions.processing.fan.CustomFanProcessingRecipe;
import com.simibubi.create.content.contraptions.processing.fan.TypeCustom;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.element.GuiGameElement;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class CustomFanCategory extends ProcessingViaFanCategory<CustomFanProcessingRecipe> {

	public CustomFanCategory() {
		super(185, doubleItemIcon(AllItems.PROPELLER.get(), Items.IRON_BARS));
	}

	@Override
	public @NotNull Class<? extends CustomFanProcessingRecipe> getRecipeClass() {
		return CustomFanProcessingRecipe.class;
	}

	@Override
	public void setIngredients(CustomFanProcessingRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutputs(VanillaTypes.ITEM, recipe.getRollableResultsAsItemStacks());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, CustomFanProcessingRecipe recipe, @Nullable IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		List<ProcessingOutput> results = recipe.getRollableResults();
		int xOffsetGlobal = 8 * (3 - Math.min(3, results.size()));

		itemStacks.init(0, true, xOffsetGlobal + 12, 47);
		itemStacks.set(0, Arrays.asList(recipe.getIngredients()
				.get(0)
				.getItems()));

		boolean single = results.size() == 1;
		boolean excessive = results.size() > 9;
		for (int outputIndex = 0; outputIndex < results.size(); outputIndex++) {
			int xOffset = (outputIndex % 3) * 19;
			int yOffset = (outputIndex / 3) * -19;

			itemStacks.init(outputIndex + 1, false, xOffsetGlobal + (single ? 126 : 126 + xOffset),
					47 + yOffset + (excessive ? 8 : 0));
			itemStacks.set(outputIndex + 1, results.get(outputIndex)
					.getStack());
		}

		addStochasticTooltip(itemStacks, results);
	}

	@Override
	protected void renderWidgets(PoseStack matrixStack, CustomFanProcessingRecipe recipe, double mouseX, double mouseY) {
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
	public void renderAttachedBlock(PoseStack matrixStack, CustomFanProcessingRecipe recipe) {
		matrixStack.pushPose();
		if (recipe.type instanceof TypeCustom custom) {
			GuiGameElement.of(Optional.ofNullable(custom.getConfig().block().getBlockForDisplay()).orElse(Blocks.AIR.defaultBlockState()))
					.scale(24)
					.atLocal(0, 0, 2)
					.lighting(AnimatedKinetics.DEFAULT_LIGHTING)
					.render(matrixStack);
		}
		matrixStack.popPose();
	}

}
