package com.jozufozu.flywheel.backend.core;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.backend.gl.GlVertexArray;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.util.AttribUtil;

public class BufferedArrayModel extends BufferedModel {

	protected GlVertexArray vao;

	public BufferedArrayModel(VertexFormat format, ByteBuffer data, int vertices) {
		super(format, data, vertices);

		vao = new GlVertexArray();

		vao.bind();

		// bind the model's vbo to our vao
		vbo.bind();
		getFormat().vertexAttribPointers(0);
		vbo.unbind();

		// enable all the attribute arrays in our vao. we only need to do this once
		AttribUtil.enableArrays(getAttributeCount());
		vao.unbind();
	}

	public void render() {
		if (vertexCount <= 0 || deleted) return;

		vao.bind();

		GL20.glDrawArrays(GL11.GL_QUADS, 0, vertexCount);

		vao.unbind();
	}

	@Override
	public void delete() {
		super.delete();
		vao.delete();
	}
}
