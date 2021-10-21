package com.simibubi.create.content.schematics.client.tools;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;

import net.minecraft.client.renderer.IRenderTypeBuffer;

public interface ISchematicTool {

	public void init();
	public void updateSelection();
	
	public boolean handleRightClick();
	public boolean handleMouseWheel(double delta);
	
	public void renderTool(MatrixStack ms, SuperRenderTypeBuffer buffer);
	public void renderOverlay(MatrixStack ms, IRenderTypeBuffer buffer);
	public void renderOnSchematic(MatrixStack ms, SuperRenderTypeBuffer buffer);
	
}
