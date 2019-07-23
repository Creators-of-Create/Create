package com.simibubi.create.modules.schematics.client.tools;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.foundation.utility.KeyboardHelper;

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
		if (blueprint.active && selectionRange == -1) {
			selectionRange = (int) blueprint.size.manhattanDistance(BlockPos.ZERO) / 2;
			selectionRange = MathHelper.clamp(selectionRange, 1, 100);
		}
		selectIgnoreBlocks = KeyboardHelper.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL);
		
		super.updateSelection();
	}
	
	@Override
	public void renderTool() {
		super.renderTool();
		
		if (selectedPos == null) 
			return;
		
		BlockPos size = blueprint.getTransformedSize();
		BlockPos min = selectedPos.add(Math.round(size.getX() * -.5f), 0, Math.round(size.getZ() * -.5f));
		BlockPos max = min.add(size.getX(), size.getY(), size.getZ());
		
		if (blueprint.deployed) {
			MutableBoundingBox bb = new MutableBoundingBox(min, min.add(blueprint.getTransformedSize()));
			min = new BlockPos(bb.minX, bb.minY, bb.minZ);
			max = new BlockPos(bb.maxX, bb.maxY, bb.maxZ);
		}
		
		GlStateManager.lineWidth(2);
		GlStateManager.color4f(.5f, .8f, 1, 1);
		GlStateManager.disableTexture();
		
		WorldRenderer.drawBoundingBox(min.getX() - 1 / 8d, min.getY() + 1 / 16d, min.getZ() - 1 / 8d,
				max.getX() + 1 / 8d, max.getY() + 1 / 8d, max.getZ() + 1 / 8d, .8f, .9f, 1, 1);
		
		GlStateManager.lineWidth(1);
		GlStateManager.enableTexture();
		
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
		
		BlockPos size = blueprint.getTransformedSize();
		blueprint.moveTo(selectedPos.add(Math.round(size.getX() * -.5f), 0, Math.round(size.getZ() * -.5f)));
		
		return true;
	}
	
}
