package com.simibubi.create.foundation.render;

public class TemplateMesh {
	public static final int INT_STRIDE = 9;
	public static final int BYTE_STRIDE = INT_STRIDE * Integer.BYTES;

	public static final int X_OFFSET = 0;
	public static final int Y_OFFSET = 1;
	public static final int Z_OFFSET = 2;
	public static final int COLOR_OFFSET = 3;
	public static final int U_OFFSET = 4;
	public static final int V_OFFSET = 5;
	public static final int OVERLAY_OFFSET = 6;
	public static final int LIGHT_OFFSET = 7;
	public static final int NORMAL_OFFSET = 8;

	protected final int[] data;
	protected final int vertexCount;

	public TemplateMesh(int[] data) {
		if (data.length % INT_STRIDE != 0) {
			throw new IllegalArgumentException("Received invalid vertex data");
		}

		this.data = data;
		vertexCount = data.length / INT_STRIDE;
	}

	public TemplateMesh(int vertexCount) {
		data = new int[vertexCount * INT_STRIDE];
		this.vertexCount = vertexCount;
	}

	public float x(int index) {
		return Float.intBitsToFloat(data[index * INT_STRIDE + X_OFFSET]);
	}

	public float y(int index) {
		return Float.intBitsToFloat(data[index * INT_STRIDE + Y_OFFSET]);
	}

	public float z(int index) {
		return Float.intBitsToFloat(data[index * INT_STRIDE + Z_OFFSET]);
	}

	// 0xAABBGGRR
	public int color(int index) {
		return data[index * INT_STRIDE + COLOR_OFFSET];
	}

	public float u(int index) {
		return Float.intBitsToFloat(data[index * INT_STRIDE + U_OFFSET]);
	}

	public float v(int index) {
		return Float.intBitsToFloat(data[index * INT_STRIDE + V_OFFSET]);
	}

	public int overlay(int index) {
		return data[index * INT_STRIDE + OVERLAY_OFFSET];
	}

	public int light(int index) {
		return data[index * INT_STRIDE + LIGHT_OFFSET];
	}

	public int normal(int index) {
		return data[index * INT_STRIDE + NORMAL_OFFSET];
	}

	public int vertexCount() {
		return vertexCount;
	}

	public boolean isEmpty() {
		return vertexCount == 0;
	}
}
