package com.simibubi.create.foundation.utility.outliner;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.foundation.utility.ColorHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class AABBOutline extends Outline {

	protected AxisAlignedBB bb;
	protected AllSpecialTextures faceTexture;
	protected AllSpecialTextures highlightedTexture;
	protected Direction highlightedFace;
	public boolean disableCull = false;

	public AABBOutline(AxisAlignedBB bb) {
		this.bb = bb;
	}

	@Override
	public void render(BufferBuilder buffer) {
		begin();

		Vec3d color = ColorHelper.getRGB(0xFFFFFF);
		float alpha = 1f;
		renderBB(bb, buffer, color, alpha, !disableCull);

		draw();
	}

	public void setTextures(AllSpecialTextures faceTexture, AllSpecialTextures highlightTexture) {
		this.faceTexture = faceTexture;
		this.highlightedTexture = highlightTexture;
	}

	public void highlightFace(Direction face) {
		this.highlightedFace = face;
	}

	public void renderBB(AxisAlignedBB bb, BufferBuilder buffer, Vec3d color, float alpha, boolean doCulling) {
		Vec3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
		boolean inside = bb.contains(projectedView);
		bb = bb.grow(inside ? -1 / 128d : 1 / 128d);

		Vec3d xyz = new Vec3d(bb.minX, bb.minY, bb.minZ);
		Vec3d Xyz = new Vec3d(bb.maxX, bb.minY, bb.minZ);
		Vec3d xYz = new Vec3d(bb.minX, bb.maxY, bb.minZ);
		Vec3d XYz = new Vec3d(bb.maxX, bb.maxY, bb.minZ);
		Vec3d xyZ = new Vec3d(bb.minX, bb.minY, bb.maxZ);
		Vec3d XyZ = new Vec3d(bb.maxX, bb.minY, bb.maxZ);
		Vec3d xYZ = new Vec3d(bb.minX, bb.maxY, bb.maxZ);
		Vec3d XYZ = new Vec3d(bb.maxX, bb.maxY, bb.maxZ);

		if (doCulling) {
			GlStateManager.enableCull();
			if (inside)
				GlStateManager.disableCull();
		}

		renderFace(Direction.NORTH, xYz, XYz, Xyz, xyz, buffer);
		renderFace(Direction.SOUTH, XYZ, xYZ, xyZ, XyZ, buffer);
		renderFace(Direction.EAST, XYz, XYZ, XyZ, Xyz, buffer);
		renderFace(Direction.WEST, xYZ, xYz, xyz, xyZ, buffer);
		renderFace(Direction.UP, xYZ, XYZ, XYz, xYz, buffer);
		renderFace(Direction.DOWN, xyz, Xyz, XyZ, xyZ, buffer);

		if (doCulling)
			GlStateManager.enableCull();

		Vec3d start = xyz;
		AllSpecialTextures.BLANK.bind();
		renderAACuboidLine(start, Xyz, color, alpha, buffer);
		renderAACuboidLine(start, xYz, color, alpha, buffer);
		renderAACuboidLine(start, xyZ, color, alpha, buffer);

		start = XyZ;
		renderAACuboidLine(start, xyZ, color, alpha, buffer);
		renderAACuboidLine(start, XYZ, color, alpha, buffer);
		renderAACuboidLine(start, Xyz, color, alpha, buffer);

		start = XYz;
		renderAACuboidLine(start, xYz, color, alpha, buffer);
		renderAACuboidLine(start, Xyz, color, alpha, buffer);
		renderAACuboidLine(start, XYZ, color, alpha, buffer);

		start = xYZ;
		renderAACuboidLine(start, XYZ, color, alpha, buffer);
		renderAACuboidLine(start, xyZ, color, alpha, buffer);
		renderAACuboidLine(start, xYz, color, alpha, buffer);

	}

	protected void renderFace(Direction direction, Vec3d p1, Vec3d p2, Vec3d p3, Vec3d p4, BufferBuilder buffer) {
		GlStateManager.texParameter(GL11.GL_TEXTURE_2D, 10242, GL11.GL_REPEAT);
		GlStateManager.texParameter(GL11.GL_TEXTURE_2D, 10243, GL11.GL_REPEAT);

		if (direction == highlightedFace && highlightedTexture != null)
			highlightedTexture.bind();
		else if (faceTexture != null)
			faceTexture.bind();
		else
			return;

		GlStateManager.depthMask(false);
		Vec3d uDiff = p2.subtract(p1);
		Vec3d vDiff = p4.subtract(p1);
		Axis axis = direction.getAxis();
		float maxU = (float) Math.abs(axis == Axis.X ? uDiff.z : uDiff.x);
		float maxV = (float) Math.abs(axis == Axis.Y ? vDiff.z : vDiff.y);

		putQuadUV(p1, p2, p3, p4, 0, 0, maxU, maxV, new Vec3d(1, 1, 1), 1, buffer);
		flush();
		GlStateManager.depthMask(true);
	}

}
