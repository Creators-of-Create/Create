package com.simibubi.create.compat.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.content.contraptions.processing.fan.HauntingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.element.GuiGameElement;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class FanHauntingCategory extends ProcessingViaFanCategory.MultiOutput<HauntingRecipe> {

	public FanHauntingCategory() {
		super(doubleItemIcon(AllItems.PROPELLER.get(), Items.SOUL_CAMPFIRE));
	}

	@Override
	public Class<? extends HauntingRecipe> getRecipeClass() {
		return HauntingRecipe.class;
	}

	@Override
	protected AllGuiTextures getBlockShadow() {
		return AllGuiTextures.JEI_LIGHT;
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
	public void renderAttachedBlock(PoseStack matrixStack, HauntingRecipe recipe) {
		matrixStack.pushPose();

		GuiGameElement.of(Blocks.SOUL_FIRE.defaultBlockState())
			.scale(SCALE)
			.atLocal(0, 0, 2)
			.lighting(AnimatedKinetics.DEFAULT_LIGHTING)
			.render(matrixStack);
	}

}
