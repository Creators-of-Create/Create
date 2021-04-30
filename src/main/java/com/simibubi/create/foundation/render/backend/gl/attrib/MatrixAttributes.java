package com.simibubi.create.foundation.render.backend.gl.attrib;

import org.lwjgl.opengl.GL20;

import com.simibubi.create.foundation.render.backend.gl.GlPrimitiveType;

public enum MatrixAttributes implements IAttribSpec {
	MAT3(3, 3),
	MAT4(4, 4),
	;

	private final int rows;
	private final int cols;

	MatrixAttributes(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
	}

	@Override
	public void vertexAttribPointer(int stride, int index, int pointer) {
		for (int i = 0; i < rows; i++) {
			long attribPointer = pointer + (long) i * cols * GlPrimitiveType.FLOAT.getSize();
			GL20.glVertexAttribPointer(index + i, cols, GlPrimitiveType.FLOAT.getGlConstant(), false, stride, attribPointer);
		}
	}

	@Override
	public int getSize() {
		return GlPrimitiveType.FLOAT.getSize() * rows * cols;
	}

	@Override
	public int getAttributeCount() {
		return rows;
	}
}
