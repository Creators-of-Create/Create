package com.simibubi.create.foundation.render;

public class MutableTemplateMesh extends TemplateMesh {
	public MutableTemplateMesh(int[] data) {
		super(data);
	}

	public MutableTemplateMesh(int vertexCount) {
		super(vertexCount);
	}

	public void copyFrom(int index, TemplateMesh template) {
		System.arraycopy(template.data, 0, data, index * INT_STRIDE, template.data.length);
	}

	public void x(int index, float x) {
		data[index * INT_STRIDE + X_OFFSET] = Float.floatToRawIntBits(x);
	}

	public void y(int index, float y) {
		data[index * INT_STRIDE + Y_OFFSET] = Float.floatToRawIntBits(y);
	}

	public void z(int index, float z) {
		data[index * INT_STRIDE + Z_OFFSET] = Float.floatToRawIntBits(z);
	}

	public void color(int index, int color) {
		data[index * INT_STRIDE + COLOR_OFFSET] = color;
	}

	public void u(int index, float u) {
		data[index * INT_STRIDE + U_OFFSET] = Float.floatToRawIntBits(u);
	}

	public void v(int index, float v) {
		data[index * INT_STRIDE + V_OFFSET] = Float.floatToRawIntBits(v);
	}

	public void overlay(int index, int overlay) {
		data[index * INT_STRIDE + OVERLAY_OFFSET] = overlay;
	}

	public void light(int index, int light) {
		data[index * INT_STRIDE + LIGHT_OFFSET] = light;
	}

	public void normal(int index, int normal) {
		data[index * INT_STRIDE + NORMAL_OFFSET] = normal;
	}

	public TemplateMesh toImmutable() {
		return new TemplateMesh(data);
	}
}
