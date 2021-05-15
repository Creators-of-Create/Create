package com.jozufozu.flywheel.backend.gl.buffer;

import org.lwjgl.opengl.GL15C;

public enum MappedBufferUsage {
	READ_ONLY(GL15C.GL_READ_ONLY),
	WRITE_ONLY(GL15C.GL_WRITE_ONLY),
	READ_WRITE(GL15C.GL_READ_WRITE),
	;

	int glEnum;

	MappedBufferUsage(int glEnum) {
		this.glEnum = glEnum;
	}
}
