package com.simibubi.create.content.schematics.client.tools;

import com.mojang.blaze3d.matrix.MatrixStack;

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
	public void renderTool(MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {
		super.renderTool(ms, buffer, light, overlay);
	}

	@Override
	public void renderOverlay(MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {
		super.renderOverlay(ms, buffer, light, overlay);
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
