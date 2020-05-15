package com.simibubi.create.foundation.utility.outliner;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.foundation.utility.ColorHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
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
	public void render(MatrixStack ms, IRenderTypeBuffer buffer) {
		Vec3d color = ColorHelper.getRGB(0xFFFFFF);
		float alpha = 1f;
		renderBB(ms, buffer, bb, color, alpha, !disableCull);
	}

	public void setTextures(AllSpecialTextures faceTexture, AllSpecialTextures highlightTexture) {
		this.faceTexture = faceTexture;
		this.highlightedTexture = highlightTexture;
	}

	public void highlightFace(Direction face) {
		this.highlightedFace = face;
	}

	public void renderBB(MatrixStack ms, IRenderTypeBuffer buffer, AxisAlignedBB bb, Vec3d color, float alpha,
		boolean doCulling) {
		Vec3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo()
			.getProjectedView();
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

		Vec3d start = xyz;
		renderAACuboidLine(ms, buffer, start, Xyz);
		renderAACuboidLine(ms, buffer, start, xYz);
		renderAACuboidLine(ms, buffer, start, xyZ);

		start = XyZ;
		renderAACuboidLine(ms, buffer, start, xyZ);
		renderAACuboidLine(ms, buffer, start, XYZ);
		renderAACuboidLine(ms, buffer, start, Xyz);

		start = XYz;
		renderAACuboidLine(ms, buffer, start, xYz);
		renderAACuboidLine(ms, buffer, start, Xyz);
		renderAACuboidLine(ms, buffer, start, XYZ);

		start = xYZ;
		renderAACuboidLine(ms, buffer, start, XYZ);
		renderAACuboidLine(ms, buffer, start, xyZ);
		renderAACuboidLine(ms, buffer, start, xYz);

		renderFace(ms, buffer, Direction.NORTH, xYz, XYz, Xyz, xyz, inside);
		renderFace(ms, buffer, Direction.SOUTH, XYZ, xYZ, xyZ, XyZ, inside);
		renderFace(ms, buffer, Direction.EAST, XYz, XYZ, XyZ, Xyz, inside);
		renderFace(ms, buffer, Direction.WEST, xYZ, xYz, xyz, xyZ, inside);
		renderFace(ms, buffer, Direction.UP, xYZ, XYZ, XYz, xYz, inside);
		renderFace(ms, buffer, Direction.DOWN, xyz, Xyz, XyZ, xyZ, inside);

	}

	protected void renderFace(MatrixStack ms, IRenderTypeBuffer buffer, Direction direction, Vec3d p1, Vec3d p2,
		Vec3d p3, Vec3d p4, boolean noCull) {

		ResourceLocation faceTexture = this.faceTexture.getLocation();
		if (direction == highlightedFace && highlightedTexture != null)
			faceTexture = highlightedTexture.getLocation();
		else if (faceTexture == null)
			return;

		RenderType translucentType =
			noCull ? RenderType.getEntityTranslucent(faceTexture) : RenderType.getEntityTranslucentCull(faceTexture);
		IVertexBuilder builder = buffer.getBuffer(translucentType);

		Axis axis = direction.getAxis();
		Vec3d uDiff = p2.subtract(p1);
		Vec3d vDiff = p4.subtract(p1);
		float maxU = (float) Math.abs(axis == Axis.X ? uDiff.z : uDiff.x);
		float maxV = (float) Math.abs(axis == Axis.Y ? vDiff.z : vDiff.y);
		putQuadUV(ms, builder, p1, p2, p3, p4, 0, 0, maxU, maxV);
	}

}
