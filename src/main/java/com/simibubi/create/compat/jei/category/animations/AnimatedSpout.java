package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.IRenderTypeBuffer.Impl;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class AnimatedSpout extends AnimatedKinetics {

	private List<FluidStack> fluids;

	public AnimatedSpout withFluids(List<FluidStack> fluids) {
		this.fluids = fluids;
		return this;
	}

	@Override
	public void draw(int xOffset, int yOffset) {
		RenderSystem.pushMatrix();
		RenderSystem.translatef(xOffset, yOffset, 100);
		RenderSystem.rotatef(-15.5f, 1, 0, 0);
		RenderSystem.rotatef(22.5f, 0, 1, 0);
		int scale = 20;

		GuiGameElement.of(AllBlocks.SPOUT.getDefaultState())
			.scale(scale)
			.render();

		float cycle = AnimationTickHolder.getRenderTick() % 30;
		float squeeze = cycle < 20 ? MathHelper.sin((float) (cycle / 20f * Math.PI)) : 0;
		squeeze *= 20;

		RenderSystem.pushMatrix();

		GuiGameElement.of(AllBlockPartials.SPOUT_TOP)
			.scale(scale)
			.render();
		RenderSystem.translatef(0, -3 * squeeze / 32f, 0);
		GuiGameElement.of(AllBlockPartials.SPOUT_MIDDLE)
			.scale(scale)
			.render();
		RenderSystem.translatef(0, -3 * squeeze / 32f, 0);
		GuiGameElement.of(AllBlockPartials.SPOUT_BOTTOM)
			.scale(scale)
			.render();
		RenderSystem.translatef(0, -3 * squeeze / 32f, 0);

		RenderSystem.popMatrix();

		GuiGameElement.of(AllBlocks.DEPOT.getDefaultState())
			.atLocal(0, 2, 0)
			.scale(scale)
			.render();

		Impl buffer = IRenderTypeBuffer.immediate(Tessellator.getInstance()
			.getBuffer());
		MatrixStack ms = new MatrixStack();
		ms.push();
		ms.scale(16, -16, 16);
		float from = 2/16f;
		float to = 1f - from;
		FluidRenderer.renderTiledFluidBB(fluids.get(0), from, from, from, to, to, to, buffer, ms, 0xf000f0, false);
		ms.pop();

		float width = 1 / 128f * squeeze;
		ms.translate(scale / 2f, scale * 1.5f, scale / 2f);
		ms.scale(16, -16, 16);
		ms.translate(-width / 2, 0, -width / 2);
		FluidRenderer.renderTiledFluidBB(fluids.get(0), 0, -0.001f, 0, width, 2.001f, width, buffer, ms, 0xf000f0,
			false);
		buffer.draw();

		RenderSystem.popMatrix();
	}

}
