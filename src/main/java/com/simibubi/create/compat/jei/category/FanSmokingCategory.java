package com.simibubi.create.compat.jei.category;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.gui.GuiGameElement;

import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.SmokingRecipe;

public class FanSmokingCategory extends ProcessingViaFanCategory<SmokingRecipe> {

	public FanSmokingCategory() {
		super(doubleItemIcon(AllItems.PROPELLER.get(), Items.BLAZE_POWDER));
	}

	@Override
	public Class<? extends SmokingRecipe> getRecipeClass() {
		return SmokingRecipe.class;
	}

	@Override
	public void renderAttachedBlock(MatrixStack matrixStack) {
		GuiGameElement.of(Blocks.FIRE.defaultBlockState())
				.scale(24)
				.atLocal(0, 0, 2)
				.render(matrixStack);
	}

}
