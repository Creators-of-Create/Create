package com.simibubi.create.foundation.utility.outliner;

import java.util.Optional;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.matrix.MatrixStack.Entry;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.Vec3d;

public abstract class Outline {

	protected OutlineParams params;
	
	public Outline() {
		params = new OutlineParams();
	}

	public abstract void render(MatrixStack ms, IRenderTypeBuffer buffer);

	public void renderAACuboidLine(MatrixStack ms, IRenderTypeBuffer buffer, Vec3d start, Vec3d end) {
		IVertexBuilder builder = buffer.getBuffer(RenderType.getEntitySolid(AllSpecialTextures.BLANK.getLocation()));

		Vec3d diff = end.subtract(start);
		if (diff.x + diff.y + diff.z < 0) {
			Vec3d temp = start;
			start = end;
			end = temp;
			diff = diff.scale(-1);
		}

		float lineWidth = params.getLineWidth();
		Vec3d extension = diff.normalize()
			.scale(lineWidth / 2);
		Vec3d plane = VecHelper.planeByNormal(diff);
		Direction face = Direction.getFacingFromVector(diff.x, diff.y, diff.z);
		Axis axis = face.getAxis();

		start = start.subtract(extension);
		end = end.add(extension);
		plane = plane.scale(lineWidth / 2);

		Vec3d a1 = plane.add(start);
		Vec3d b1 = plane.add(end);
		plane = VecHelper.rotate(plane, -90, axis);
		Vec3d a2 = plane.add(start);
		Vec3d b2 = plane.add(end);
		plane = VecHelper.rotate(plane, -90, axis);
		Vec3d a3 = plane.add(start);
		Vec3d b3 = plane.add(end);
		plane = VecHelper.rotate(plane, -90, axis);
		Vec3d a4 = plane.add(start);
		Vec3d b4 = plane.add(end);

		putQuad(ms, builder, b4, b3, b2, b1);
		putQuad(ms, builder, a1, a2, a3, a4);
		putQuad(ms, builder, a1, b1, b2, a2);
		putQuad(ms, builder, a2, b2, b3, a3);
		putQuad(ms, builder, a3, b3, b4, a4);
		putQuad(ms, builder, a4, b4, b1, a1);
	}

	public void putQuad(MatrixStack ms, IVertexBuilder builder, Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4) {
		putQuadUV(ms, builder, v1, v2, v3, v4, 0, 0, 1, 1);
	}

	public void putQuadUV(MatrixStack ms, IVertexBuilder builder, Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, float minU,
		float minV, float maxU, float maxV) {
		putVertex(ms, builder, v1, minU, minV);
		putVertex(ms, builder, v2, maxU, minV);
		putVertex(ms, builder, v3, maxU, maxV);
		putVertex(ms, builder, v4, minU, maxV);
	}

	protected void putVertex(MatrixStack ms, IVertexBuilder builder, Vec3d pos, float u, float v) {
		int i = 15 << 20 | 15 << 4;
		int j = i >> 16 & '\uffff';
		int k = i & '\uffff';
		Entry peek = ms.peek();
		Vec3d rgb = params.rgb;

		builder.vertex(peek.getModel(), (float) pos.x, (float) pos.y, (float) pos.z)
			.color((float) rgb.x, (float) rgb.y, (float) rgb.z, params.alpha)
			.texture(u, v)
			.overlay(OverlayTexture.DEFAULT_UV)
			.light(j, k)
			.normal(peek.getNormal(), 0, 1, 0)
			.endVertex();
	}

	public void tick() {}
	
	public OutlineParams getParams() {
		return params;
	}

	public static class OutlineParams {
		Optional<AllSpecialTextures> faceTexture;
		Optional<AllSpecialTextures> hightlightedFaceTexture;
		boolean fadeLineWidth;
		float alpha;
		private float lineWidth;
		int lightMapU, lightMapV;
		Vec3d rgb;

		public OutlineParams() {
			faceTexture = hightlightedFaceTexture = Optional.empty();
			alpha = 1;
			lineWidth = 1 / 32f;
			fadeLineWidth = true;
			rgb = ColorHelper.getRGB(0xFFFFFF);

			int i = 15 << 20 | 15 << 4;
			lightMapU = i >> 16 & '\uffff';
			lightMapV = i & '\uffff';
		}
		
		// builder
		
		public OutlineParams colored(int color) {
			rgb = ColorHelper.getRGB(color);
			return this;
		}
		
		public OutlineParams lineWidth(float width) {
			this.lineWidth = width;
			return this;
		}
		
		public OutlineParams withFaceTexture(AllSpecialTextures texture) {
			this.faceTexture = Optional.of(texture);
			return this;
		}
		
		// util
		
		float getLineWidth() {
			return fadeLineWidth ? alpha * lineWidth : lineWidth;
		}
		
	}

}
