package com.simibubi.create.modules.schematics.client.tools;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;

public interface ISchematicTool {

	public void init();
	public void updateSelection();
	
	public boolean handleRightClick();
	public boolean handleMouseWheel(double delta);
	
	public void renderTool(MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay);
	public void renderOverlay(MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay);
	
	
}
