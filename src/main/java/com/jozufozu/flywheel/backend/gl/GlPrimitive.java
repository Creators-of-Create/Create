package com.jozufozu.flywheel.backend.gl;

import org.lwjgl.opengl.GL11;

public enum GlPrimitive {
	POINTS(GL11.GL_POINTS),
	LINES(GL11.GL_LINES),
	LINE_LOOP(GL11.GL_LINE_LOOP),
	LINE_STRIP(GL11.GL_LINE_STRIP),
	TRIANGLES(GL11.GL_TRIANGLES),
	TRIANGLE_STRIP(GL11.GL_TRIANGLE_STRIP),
	TRIANGLE_FAN(GL11.GL_TRIANGLE_FAN),
	QUADS(GL11.GL_QUADS),
	QUAD_STRIP(GL11.GL_QUAD_STRIP),
	POLYGON(GL11.GL_POLYGON),
	;

	public final int glEnum;

	GlPrimitive(int glEnum) {
		this.glEnum = glEnum;
	}
}
