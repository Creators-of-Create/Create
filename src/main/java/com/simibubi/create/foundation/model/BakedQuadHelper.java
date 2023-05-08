package com.simibubi.create.foundation.model;

import java.util.Arrays;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.world.phys.Vec3;

public final class BakedQuadHelper {

	public static final VertexFormat FORMAT = DefaultVertexFormat.BLOCK;
	public static final int VERTEX_STRIDE = FORMAT.getIntegerSize();

	public static final int X_OFFSET = 0;
	public static final int Y_OFFSET = 1;
	public static final int Z_OFFSET = 2;
	public static final int COLOR_OFFSET = 3;
	public static final int U_OFFSET = 4;
	public static final int V_OFFSET = 5;
	public static final int LIGHT_OFFSET = 6;
	public static final int NORMAL_OFFSET = 7;

	private BakedQuadHelper() {}

	public static BakedQuad clone(BakedQuad quad) {
		return new BakedQuad(Arrays.copyOf(quad.getVertices(), quad.getVertices().length),
			quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
	}

	public static BakedQuad cloneWithCustomGeometry(BakedQuad quad, int[] vertexData) {
		return new BakedQuad(vertexData, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
	}

	public static Vec3 getXYZ(int[] vertexData, int vertex) {
		float x = Float.intBitsToFloat(vertexData[vertex * VERTEX_STRIDE + X_OFFSET]);
        float y = Float.intBitsToFloat(vertexData[vertex * VERTEX_STRIDE + Y_OFFSET]);
        float z = Float.intBitsToFloat(vertexData[vertex * VERTEX_STRIDE + Z_OFFSET]);
        return new Vec3(x, y, z);
	}

	public static void setXYZ(int[] vertexData, int vertex, Vec3 xyz) {
		vertexData[vertex * VERTEX_STRIDE + X_OFFSET] = Float.floatToRawIntBits((float) xyz.x);
		vertexData[vertex * VERTEX_STRIDE + Y_OFFSET] = Float.floatToRawIntBits((float) xyz.y);
		vertexData[vertex * VERTEX_STRIDE + Z_OFFSET] = Float.floatToRawIntBits((float) xyz.z);
	}

	public static float getU(int[] vertexData, int vertex) {
		return Float.intBitsToFloat(vertexData[vertex * VERTEX_STRIDE + U_OFFSET]);
	}

	public static float getV(int[] vertexData, int vertex) {
		return Float.intBitsToFloat(vertexData[vertex * VERTEX_STRIDE + V_OFFSET]);
	}

	public static void setU(int[] vertexData, int vertex, float u) {
		vertexData[vertex * VERTEX_STRIDE + U_OFFSET] = Float.floatToRawIntBits(u);
	}

	public static void setV(int[] vertexData, int vertex, float v) {
		vertexData[vertex * VERTEX_STRIDE + V_OFFSET] = Float.floatToRawIntBits(v);
	}

}
