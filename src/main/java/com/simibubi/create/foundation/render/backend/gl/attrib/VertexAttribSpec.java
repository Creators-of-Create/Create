package com.simibubi.create.foundation.render.backend.gl.attrib;

import org.lwjgl.opengl.GL20;

import com.simibubi.create.foundation.render.backend.gl.GlPrimitiveType;

public class VertexAttribSpec implements IAttribSpec {

	private final GlPrimitiveType type;
	private final int count;
	private final int size;
	private final int attributeCount;
	private final boolean normalized;

	public VertexAttribSpec(GlPrimitiveType type, int count) {
		this(type, count, false);
	}

	public VertexAttribSpec(GlPrimitiveType type, int count, boolean normalized) {
		this.type = type;
		this.count = count;
		this.size = type.getSize() * count;
		this.attributeCount = (this.size + 15) / 16; // ceiling division. GLSL vertex attributes can only be 16 bytes wide
		this.normalized = normalized;
	}

	@Override
	public void vertexAttribPointer(int stride, int index, int pointer) {
		GL20.glVertexAttribPointer(index, count, type.getGlConstant(), normalized, stride, pointer);
	}

	@Override
	public int getSize() {
		return size;
	}

	@Override
	public int getAttributeCount() {
		return attributeCount;
	}
}
