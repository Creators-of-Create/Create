package com.simibubi.create.modules.schematics.client.tools;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.outliner.AABBOutline;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class FlipTool extends PlacementToolBase {

	@Override
	public void init() {
		super.init();
		renderSelectedFace = false;
	}

	@Override
	public boolean handleRightClick() {
		mirror();
		return true;
	}

	@Override
	public boolean handleMouseWheel(double delta) {
		mirror();
		return true;
	}

	@Override
	public void updateSelection() {
		super.updateSelection();
	}

	private void mirror() {
		if (schematicSelected && selectedFace.getAxis().isHorizontal()) {
			schematicHandler.getTransformation().flip(selectedFace.getAxis());
			schematicHandler.markDirty();
		}
	}

	@Override
	public void renderToolLocal() {
		super.renderToolLocal();

		if (!schematicSelected || !selectedFace.getAxis().isHorizontal())
			return;

		GlStateManager.pushMatrix();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.enableBlend();

		Direction facing = selectedFace.rotateY();
		Axis axis = facing.getAxis();
		Vec3d color = ColorHelper.getRGB(0x4d80e4);
		AxisAlignedBB bounds = schematicHandler.getBounds();

		Vec3d plane = VecHelper.planeByNormal(new Vec3d(facing.getDirectionVec()));
		plane = plane.mul(bounds.getXSize() / 2f + 1, bounds.getYSize() / 2f + 1, bounds.getZSize() / 2f + 1);
		Vec3d center = bounds.getCenter();

		Vec3d v1 = plane.add(center);
		plane = plane.mul(-1, 1, -1);
		Vec3d v2 = plane.add(center);
		plane = plane.mul(1, -1, 1);
		Vec3d v3 = plane.add(center);
		plane = plane.mul(-1, 1, -1);
		Vec3d v4 = plane.add(center);

		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
		AABBOutline outline = schematicHandler.getOutline();
		AllSpecialTextures.BLANK.bind();
		outline.renderAACuboidLine(v1, v2, color, 1, buffer);
		outline.renderAACuboidLine(v2, v3, color, 1, buffer);
		outline.renderAACuboidLine(v3, v4, color, 1, buffer);
		outline.renderAACuboidLine(v4, v1, color, 1, buffer);

		Tessellator.getInstance().draw();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

		GlStateManager.texParameter(GL11.GL_TEXTURE_2D, 10242, GL11.GL_REPEAT);
		GlStateManager.texParameter(GL11.GL_TEXTURE_2D, 10243, GL11.GL_REPEAT);
		GlStateManager.depthMask(false);
		Vec3d uDiff = v2.subtract(v1);
		Vec3d vDiff = v4.subtract(v1);
		float maxU = (float) Math.abs(axis == Axis.X ? uDiff.z : uDiff.x);
		float maxV = (float) Math.abs(axis == Axis.Y ? vDiff.z : vDiff.y);

		GlStateManager.enableCull();
		AllSpecialTextures.HIGHLIGHT_CHECKERED.bind();
		outline.putQuadUV(v1, v2, v3, v4, 0, 0, maxU, maxV, color, 1, buffer);
		outline.putQuadUV(v2, v1, v4, v3, 0, 0, maxU, maxV, color, 1, buffer);
		Tessellator.getInstance().draw();
		GlStateManager.popMatrix();
	}

}
