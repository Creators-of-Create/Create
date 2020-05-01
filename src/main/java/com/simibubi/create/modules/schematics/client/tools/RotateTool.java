package com.simibubi.create.modules.schematics.client.tools;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;

public class RotateTool extends PlacementToolBase {

	@Override
	public boolean handleMouseWheel(double delta) {
		schematicHandler.getTransformation().rotate90(delta > 0);
		schematicHandler.markDirty();
		return true;
	}

	@Override
	public void renderToolLocal(MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {
		super.renderToolLocal(ms, buffer, light, overlay);

//		GlStateManager.pushMatrix();TODO 1.15
//		RenderHelper.disableStandardItemLighting();
//		GlStateManager.enableBlend();
//
//		Vec3d color = ColorHelper.getRGB(0x4d80e4);
//		AxisAlignedBB bounds = schematicHandler.getBounds();
//		double height = bounds.getYSize() + Math.max(20, bounds.getYSize());
//
//		Vec3d center = bounds.getCenter().add(schematicHandler.getTransformation().getRotationOffset(false));
//		Vec3d start = center.subtract(0, height / 2, 0);
//		Vec3d end = center.add(0, height / 2, 0);
//
//		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
//		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
//		schematicHandler.getOutline().renderAACuboidLine(start, end, color, 1, buffer);
//		Tessellator.getInstance().draw();
//		GlStateManager.popMatrix();
	}

}
