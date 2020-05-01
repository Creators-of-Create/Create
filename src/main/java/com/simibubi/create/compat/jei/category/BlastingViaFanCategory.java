package com.simibubi.create.compat.jei.category;

import com.mojang.blaze3d.systems.RenderSystem;
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
		RenderSystem.pushMatrix();
		RenderSystem.translated(0, 0, 200);
		RenderSystem.enableRescaleNormal();
		
		RenderSystem.pushMatrix();
		RenderSystem.translated(0, 200, 0);
		RenderSystem.rotatef(90, 1, 0, 0);
		ScreenElementRenderer.renderBlock(() -> state);
		RenderSystem.popMatrix();

		RenderSystem.pushMatrix();
		RenderSystem.translated(0, 200, 0);
		RenderSystem.rotatef(90, 1, 0, 0);
		RenderSystem.rotatef(270, 0, 0, 1);
		ScreenElementRenderer.renderBlock(() -> state);
		RenderSystem.popMatrix();

		RenderSystem.pushMatrix();
		RenderSystem.translated(-103, -100, 0);
		ScreenElementRenderer.renderBlock(() -> state);
		RenderSystem.popMatrix();

		RenderSystem.popMatrix();
	}

}
