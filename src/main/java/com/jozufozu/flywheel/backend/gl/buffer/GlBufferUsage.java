package com.jozufozu.flywheel.backend.gl.buffer;

import org.lwjgl.opengl.GL15;

public enum GlBufferUsage {
	STREAM_DRAW(GL15.GL_STREAM_DRAW),
	STREAM_READ(GL15.GL_STREAM_READ),
	STREAM_COPY(GL15.GL_STREAM_COPY),
	STATIC_DRAW(GL15.GL_STATIC_DRAW),
	STATIC_READ(GL15.GL_STATIC_READ),
	STATIC_COPY(GL15.GL_STATIC_COPY),
	DYNAMIC_DRAW(GL15.GL_DYNAMIC_DRAW),
	DYNAMIC_READ(GL15.GL_DYNAMIC_READ),
	DYNAMIC_COPY(GL15.GL_DYNAMIC_COPY),
	;

	public final int glEnum;

	GlBufferUsage(int glEnum) {
		this.glEnum = glEnum;
	}
}
