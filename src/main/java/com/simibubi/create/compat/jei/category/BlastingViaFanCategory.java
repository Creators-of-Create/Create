package com.simibubi.create.compat.jei.category;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.gui.GuiGameElement;

import net.minecraft.fluid.Fluids;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.AbstractCookingRecipe;

public class BlastingViaFanCategory extends ProcessingViaFanCategory<AbstractCookingRecipe> {

	public BlastingViaFanCategory() {
		super("blasting_via_fan", doubleItemIcon(AllItems.PROPELLER.get(), Items.LAVA_BUCKET));
	}

	@Override
	public Class<? extends AbstractCookingRecipe> getRecipeClass() {
		return AbstractCookingRecipe.class;
	}

	@Override
	public void renderAttachedBlock(MatrixStack matrixStack) {
		matrixStack.push();

		GuiGameElement.of(Fluids.LAVA)
				.scale(24)
				.atLocal(0, 0, 2)
				.render();

		matrixStack.pop();
	}

}
