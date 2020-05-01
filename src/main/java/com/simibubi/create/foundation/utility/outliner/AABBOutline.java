package com.simibubi.create.foundation.utility.outliner;

import com.mojang.blaze3d.vertex.IVertexBuilder;
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
		Vec3d color = ColorHelper.getRGB(0xFFFFFF);
		float alpha = 1f;
		renderBB(bb, buffer, color, alpha, !disableCull);
	}

	public void setTextures(AllSpecialTextures faceTexture, AllSpecialTextures highlightTexture) {
		this.faceTexture = faceTexture;
		this.highlightedTexture = highlightTexture;
	}

	public void highlightFace(Direction face) {
		this.highlightedFace = face;
	}

	public void renderBB(AxisAlignedBB bb, IVertexBuilder builder, Vec3d color, float alpha, boolean doCulling) {
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

		renderFace(Direction.NORTH, xYz, XYz, Xyz, xyz, builder);
		renderFace(Direction.SOUTH, XYZ, xYZ, xyZ, XyZ, builder);
		renderFace(Direction.EAST, XYz, XYZ, XyZ, Xyz, builder);
		renderFace(Direction.WEST, xYZ, xYz, xyz, xyZ, builder);
		renderFace(Direction.UP, xYZ, XYZ, XYz, xYz, builder);
		renderFace(Direction.DOWN, xyz, Xyz, XyZ, xyZ, builder);

		Vec3d start = xyz;
		AllSpecialTextures.BLANK.bind();
		renderAACuboidLine(start, Xyz, color, alpha, builder);
		renderAACuboidLine(start, xYz, color, alpha, builder);
		renderAACuboidLine(start, xyZ, color, alpha, builder);

		start = XyZ;
		renderAACuboidLine(start, xyZ, color, alpha, builder);
		renderAACuboidLine(start, XYZ, color, alpha, builder);
		renderAACuboidLine(start, Xyz, color, alpha, builder);

		start = XYz;
		renderAACuboidLine(start, xYz, color, alpha, builder);
		renderAACuboidLine(start, Xyz, color, alpha, builder);
		renderAACuboidLine(start, XYZ, color, alpha, builder);

		start = xYZ;
		renderAACuboidLine(start, XYZ, color, alpha, builder);
		renderAACuboidLine(start, xyZ, color, alpha, builder);
		renderAACuboidLine(start, xYz, color, alpha, builder);

	}

	protected void renderFace(Direction direction, Vec3d p1, Vec3d p2, Vec3d p3, Vec3d p4, IVertexBuilder builder) {
		if (direction == highlightedFace && highlightedTexture != null)
			highlightedTexture.bind();
		else if (faceTexture != null)
			faceTexture.bind();
		else
			return;

		Vec3d uDiff = p2.subtract(p1);
		Vec3d vDiff = p4.subtract(p1);
		Axis axis = direction.getAxis();
		float maxU = (float) Math.abs(axis == Axis.X ? uDiff.z : uDiff.x);
		float maxV = (float) Math.abs(axis == Axis.Y ? vDiff.z : vDiff.y);
		putQuadUV(p1, p2, p3, p4, 0, 0, maxU, maxV, new Vec3d(1, 1, 1), 1, builder);
	}

}
