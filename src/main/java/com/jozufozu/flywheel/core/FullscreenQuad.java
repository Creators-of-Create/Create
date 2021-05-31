package com.jozufozu.flywheel.core;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.backend.gl.GlNumericType;
import com.jozufozu.flywheel.backend.gl.GlVertexArray;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;

import net.minecraftforge.common.util.Lazy;

public class FullscreenQuad {

	public static final Lazy<FullscreenQuad> INSTANCE = Lazy.of(FullscreenQuad::new);

	private static final float[] vertices = {
			// pos          // tex
			-1.0f, -1.0f, 0.0f, 0.0f,
			1.0f, 1.0f, 1.0f, 1.0f,
			-1.0f, 1.0f, 0.0f, 1.0f,

			-1.0f, -1.0f, 0.0f, 0.0f,
			1.0f, -1.0f, 1.0f, 0.0f,
			1.0f, 1.0f, 1.0f, 1.0f
	};

	private static final int bufferSize = vertices.length * 4;

	private final GlVertexArray vao;
	private final GlBuffer vbo;

	private FullscreenQuad() {
		vbo = new GlBuffer(GlBufferType.ARRAY_BUFFER);
		vbo.bind();
		vbo.alloc(bufferSize);
		vbo.getBuffer(0, bufferSize)
				.putFloatArray(vertices)
				.flush();

		vao = new GlVertexArray();
		vao.bind();

		GL20.glEnableVertexAttribArray(0);

		GL20.glVertexAttribPointer(0, 4, GlNumericType.FLOAT.getGlEnum(), false, 4 * 4, 0);

		vao.unbind();
		vbo.unbind();
	}

	public void draw() {
		vao.bind();
		GL20.glDrawArrays(GL20.GL_TRIANGLES, 0, 6);
		vao.unbind();
	}

	public void delete() {
		vao.delete();
		vbo.delete();
	}
}
