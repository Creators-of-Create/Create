package com.simibubi.create.content.schematics.client.tools;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;

import net.minecraft.client.renderer.IRenderTypeBuffer;

public abstract class PlacementToolBase extends SchematicToolBase {

	@Override
	public void init() {
		super.init();
	}

	@Override
	public void updateSelection() {
		super.updateSelection();
	}

	@Override
	public void renderTool(MatrixStack ms, SuperRenderTypeBuffer buffer) {
		super.renderTool(ms, buffer);
	}

	@Override
	public void renderOverlay(MatrixStack ms, IRenderTypeBuffer buffer) {
		super.renderOverlay(ms, buffer);
	}

	@Override
	public boolean handleMouseWheel(double delta) {
		return false;
	}

	@Override
	public boolean handleRightClick() {
		return false;
	}

}
