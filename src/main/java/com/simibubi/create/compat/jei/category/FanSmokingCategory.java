package com.simibubi.create.compat.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.gui.element.GuiGameElement;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.block.Blocks;

public class FanSmokingCategory extends ProcessingViaFanCategory<SmokingRecipe> {

	public FanSmokingCategory() {
		super(doubleItemIcon(AllItems.PROPELLER.get(), Items.CAMPFIRE));
	}

	@Override
	public Class<? extends SmokingRecipe> getRecipeClass() {
		return SmokingRecipe.class;
	}

	@Override
	public void renderAttachedBlock(PoseStack matrixStack) {
		GuiGameElement.of(Blocks.FIRE.defaultBlockState())
				.scale(24)
				.atLocal(0, 0, 2)
				.render(matrixStack);
	}

}
