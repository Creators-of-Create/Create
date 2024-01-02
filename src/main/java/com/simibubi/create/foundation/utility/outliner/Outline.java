package com.simibubi.create.foundation.utility.outliner;

import java.util.Optional;

import javax.annotation.Nullable;

import org.joml.Matrix3f;

import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.foundation.render.RenderTypes;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public abstract class Outline {

	protected OutlineParams params;
	protected Matrix3f transformNormals; // TODO: not used?

	public Outline() {
		params = new OutlineParams();
	}

	public abstract void render(PoseStack ms, SuperRenderTypeBuffer buffer, float pt);

	public void tick() {}

	public OutlineParams getParams() {
		return params;
	}

	public void renderCuboidLine(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 start, Vec3 end) {
		Vec3 diff = end.subtract(start);
		float hAngle = AngleHelper.deg(Mth.atan2(diff.x, diff.z));
		float hDistance = (float) diff.multiply(1, 0, 1)
			.length();
		float vAngle = AngleHelper.deg(Mth.atan2(hDistance, diff.y)) - 90;
		ms.pushPose();
		TransformStack.of(ms)
			.translate(start)
			.rotateY(hAngle).rotateX(vAngle);
		renderAACuboidLine(ms, buffer, Vec3.ZERO, new Vec3(0, 0, diff.length()));
		ms.popPose();
	}

	public void renderAACuboidLine(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 start, Vec3 end) {
		float lineWidth = params.getLineWidth();
		if (lineWidth == 0)
			return;

		VertexConsumer builder = buffer.getBuffer(RenderTypes.getOutlineSolid());

		Vec3 diff = end.subtract(start);
		if (diff.x + diff.y + diff.z < 0) {
			Vec3 temp = start;
			start = end;
			end = temp;
			diff = diff.scale(-1);
		}

		Vec3 extension = diff.normalize()
			.scale(lineWidth / 2);
		Vec3 plane = VecHelper.axisAlingedPlaneOf(diff);
		Direction face = Direction.getNearest(diff.x, diff.y, diff.z);
		Axis axis = face.getAxis();

		start = start.subtract(extension);
		end = end.add(extension);
		plane = plane.scale(lineWidth / 2);

		Vec3 a1 = plane.add(start);
		Vec3 b1 = plane.add(end);
		plane = VecHelper.rotate(plane, -90, axis);
		Vec3 a2 = plane.add(start);
		Vec3 b2 = plane.add(end);
		plane = VecHelper.rotate(plane, -90, axis);
		Vec3 a3 = plane.add(start);
		Vec3 b3 = plane.add(end);
		plane = VecHelper.rotate(plane, -90, axis);
		Vec3 a4 = plane.add(start);
		Vec3 b4 = plane.add(end);

		if (params.disableNormals) {
			face = Direction.UP;
			putQuad(ms, builder, b4, b3, b2, b1, face);
			putQuad(ms, builder, a1, a2, a3, a4, face);
			putQuad(ms, builder, a1, b1, b2, a2, face);
			putQuad(ms, builder, a2, b2, b3, a3, face);
			putQuad(ms, builder, a3, b3, b4, a4, face);
			putQuad(ms, builder, a4, b4, b1, a1, face);
			return;
		}

		putQuad(ms, builder, b4, b3, b2, b1, face);
		putQuad(ms, builder, a1, a2, a3, a4, face.getOpposite());
		Vec3 vec = a1.subtract(a4);
		face = Direction.getNearest(vec.x, vec.y, vec.z);
		putQuad(ms, builder, a1, b1, b2, a2, face);
		vec = VecHelper.rotate(vec, -90, axis);
		face = Direction.getNearest(vec.x, vec.y, vec.z);
		putQuad(ms, builder, a2, b2, b3, a3, face);
		vec = VecHelper.rotate(vec, -90, axis);
		face = Direction.getNearest(vec.x, vec.y, vec.z);
		putQuad(ms, builder, a3, b3, b4, a4, face);
		vec = VecHelper.rotate(vec, -90, axis);
		face = Direction.getNearest(vec.x, vec.y, vec.z);
		putQuad(ms, builder, a4, b4, b1, a1, face);
	}

	public void putQuad(PoseStack ms, VertexConsumer builder, Vec3 v1, Vec3 v2, Vec3 v3, Vec3 v4,
		Direction normal) {
		putQuadUV(ms, builder, v1, v2, v3, v4, 0, 0, 1, 1, normal);
	}

	public void putQuadUV(PoseStack ms, VertexConsumer builder, Vec3 v1, Vec3 v2, Vec3 v3, Vec3 v4, float minU,
		float minV, float maxU, float maxV, Direction normal) {
		putVertex(ms, builder, v1, minU, minV, normal);
		putVertex(ms, builder, v2, maxU, minV, normal);
		putVertex(ms, builder, v3, maxU, maxV, normal);
		putVertex(ms, builder, v4, minU, maxV, normal);
	}

	protected void putVertex(PoseStack ms, VertexConsumer builder, Vec3 pos, float u, float v, Direction normal) {
		putVertex(ms.last(), builder, (float) pos.x, (float) pos.y, (float) pos.z, u, v, normal);
	}

	protected void putVertex(PoseStack.Pose pose, VertexConsumer builder, float x, float y, float z, float u, float v, Direction normal) {
		Color rgb = params.rgb;
		if (transformNormals == null)
			transformNormals = pose.normal();

		int xOffset = 0;
		int yOffset = 0;
		int zOffset = 0;

		if (normal != null) {
			xOffset = normal.getStepX();
			yOffset = normal.getStepY();
			zOffset = normal.getStepZ();
		}

		builder.vertex(pose.pose(), x, y, z)
			.color(rgb.getRedAsFloat(), rgb.getGreenAsFloat(), rgb.getBlueAsFloat(), rgb.getAlphaAsFloat() * params.alpha)
			.uv(u, v)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(params.lightMap)
			.normal(pose.normal(), xOffset, yOffset, zOffset)
			.endVertex();

		transformNormals = null;
	}

	public static class OutlineParams {
		protected Optional<AllSpecialTextures> faceTexture;
		protected Optional<AllSpecialTextures> hightlightedFaceTexture;
		protected Direction highlightedFace;
		protected boolean fadeLineWidth;
		protected boolean disableCull;
		protected boolean disableNormals;
		protected float alpha;
		protected int lightMap;
		protected Color rgb;
		private float lineWidth;

		public OutlineParams() {
			faceTexture = hightlightedFaceTexture = Optional.empty();
			alpha = 1;
			lineWidth = 1 / 32f;
			fadeLineWidth = true;
			rgb = Color.WHITE;
			lightMap = LightTexture.FULL_BRIGHT;
		}

		// builder

		public OutlineParams colored(int color) {
			rgb = new Color(color, false);
			return this;
		}

		public OutlineParams colored(Color c) {
			rgb = c.copy();
			return this;
		}

		public OutlineParams lightMap(int light) {
			lightMap = light;
			return this;
		}

		public OutlineParams lineWidth(float width) {
			this.lineWidth = width;
			return this;
		}

		public OutlineParams withFaceTexture(AllSpecialTextures texture) {
			this.faceTexture = Optional.ofNullable(texture);
			return this;
		}

		public OutlineParams clearTextures() {
			return this.withFaceTextures(null, null);
		}

		public OutlineParams withFaceTextures(AllSpecialTextures texture, AllSpecialTextures highlightTexture) {
			this.faceTexture = Optional.ofNullable(texture);
			this.hightlightedFaceTexture = Optional.ofNullable(highlightTexture);
			return this;
		}

		public OutlineParams highlightFace(@Nullable Direction face) {
			highlightedFace = face;
			return this;
		}

		public OutlineParams disableNormals() {
			disableNormals = true;
			return this;
		}

		public OutlineParams disableCull() {
			disableCull = true;
			return this;
		}

		// getter

		public float getLineWidth() {
			return fadeLineWidth ? alpha * lineWidth : lineWidth;
		}

		public Direction getHighlightedFace() {
			return highlightedFace;
		}

	}

}
