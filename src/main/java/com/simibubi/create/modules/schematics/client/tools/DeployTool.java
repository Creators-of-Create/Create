package com.simibubi.create.modules.schematics.client.tools;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllKeys;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.MutableBoundingBox;

public class DeployTool extends PlacementToolBase {

	@Override
	public void init() {
		super.init();
		selectionRange = -1;
	}
	
	@Override
	public void updateSelection() {
		if (schematicHandler.active && selectionRange == -1) {
			selectionRange = (int) schematicHandler.size.manhattanDistance(BlockPos.ZERO) / 2;
			selectionRange = MathHelper.clamp(selectionRange, 1, 100);
		}
		selectIgnoreBlocks = AllKeys.ACTIVATE_TOOL.isPressed();
		
		super.updateSelection();
	}
	
	@Override
	public void renderTool(MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {
		super.renderTool(ms, buffer, light, overlay);
		
		if (selectedPos == null) 
			return;
		
		BlockPos size = schematicHandler.getTransformedSize();
		BlockPos min = selectedPos.add(Math.round(size.getX() * -.5f), 0, Math.round(size.getZ() * -.5f));
		BlockPos max = min.add(size.getX(), size.getY(), size.getZ());
		
		if (schematicHandler.deployed) {
			MutableBoundingBox bb = new MutableBoundingBox(min, min.add(schematicHandler.getTransformedSize()));
			min = new BlockPos(bb.minX, bb.minY, bb.minZ);
			max = new BlockPos(bb.maxX, bb.maxY, bb.maxZ);
		}
		
//		RenderSystem.color4f(.5f, .8f, 1, 1); TODO 1.15
		
		WorldRenderer.drawBox(ms, buffer.getBuffer(RenderType.getLines()), min.getX() - 1 / 8d, min.getY() + 1 / 16d, min.getZ() - 1 / 8d,
				max.getX() + 1 / 8d, max.getY() + 1 / 8d, max.getZ() + 1 / 8d, .8f, .9f, 1, 1);
	}
	
	@Override
	public boolean handleMouseWheel(double delta) {
		
		if (selectIgnoreBlocks) {
			selectionRange += delta;
			selectionRange = MathHelper.clamp(selectionRange, 1, 100);
			return true;
		}
		
		return super.handleMouseWheel(delta);
	}
	
	@Override
	public boolean handleRightClick() {
		if (selectedPos == null)
			return super.handleRightClick();
		
		BlockPos size = schematicHandler.getTransformedSize();
		schematicHandler.moveTo(selectedPos.add(Math.round(size.getX() * -.5f), 0, Math.round(size.getZ() * -.5f)));
		
		return true;
	}
	
}
