package com.simibubi.create.compat.jei.category;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.gui.ScreenElementRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
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
	public void renderAttachedBlock() {
		BlockState state = Blocks.LAVA.getDefaultState().with(FlowingFluidBlock.LEVEL, 8);
		GlStateManager.pushMatrix();
		GlStateManager.translated(0, 0, 200);
		GlStateManager.enableRescaleNormal();
		
		GlStateManager.pushMatrix();
		GlStateManager.translated(0, 200, 0);
		GlStateManager.rotated(90, 1, 0, 0);
		ScreenElementRenderer.renderBlock(() -> state);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translated(0, 200, 0);
		GlStateManager.rotated(90, 1, 0, 0);
		GlStateManager.rotated(270, 0, 0, 1);
		ScreenElementRenderer.renderBlock(() -> state);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translated(-103, -100, 0);
		ScreenElementRenderer.renderBlock(() -> state);
		GlStateManager.popMatrix();

		GlStateManager.popMatrix();
	}

}
