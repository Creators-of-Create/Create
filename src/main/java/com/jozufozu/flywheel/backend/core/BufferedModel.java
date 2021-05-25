package com.jozufozu.flywheel.backend.core;

import static org.lwjgl.opengl.GL20.GL_COLOR_ARRAY;
import static org.lwjgl.opengl.GL20.GL_INDEX_ARRAY;
import static org.lwjgl.opengl.GL20.GL_NORMAL_ARRAY;
import static org.lwjgl.opengl.GL20.GL_QUADS;
import static org.lwjgl.opengl.GL20.GL_TEXTURE_COORD_ARRAY;
import static org.lwjgl.opengl.GL20.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL20.glDisableClientState;
import static org.lwjgl.opengl.GL20.glDrawArrays;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.util.AttribUtil;

public class BufferedModel {

	protected final ByteBuffer data;
	protected final VertexFormat format;
	protected final int vertexCount;
	protected GlBuffer vbo;
	protected boolean deleted;

	public BufferedModel(VertexFormat format, ByteBuffer data, int vertices) {
		this.data = data;
		this.format = format;
		this.vertexCount = vertices;

		vbo = new GlBuffer(GlBufferType.ARRAY_BUFFER);

		vbo.bind();
		// allocate the buffer on the gpu
		vbo.alloc(this.data.capacity());

		// mirror it in system memory so we can write to it, and upload our model.
		vbo.getBuffer(0, this.data.capacity())
				.put(this.data)
				.flush();
		vbo.unbind();
	}

	public VertexFormat getFormat() {
		return format;
	}

	public int getVertexCount() {
		return vertexCount;
	}

	public void bindBuffer() {
		vbo.bind();
	}

	public void unbindBuffer() {
		vbo.unbind();
	}

	/**
	 * Renders this model, checking first if there is anything to render.
	 */
	public void render() {
		if (vertexCount <= 0 || deleted) return;

		// TODO: minecraft sometimes leaves its state dirty on launch. this is a hack
		glDisableClientState(GL_VERTEX_ARRAY);
		glDisableClientState(GL_NORMAL_ARRAY);
		glDisableClientState(GL_COLOR_ARRAY);
		glDisableClientState(GL_INDEX_ARRAY);
		glDisableClientState(GL_TEXTURE_COORD_ARRAY);

		vbo.bind();

		AttribUtil.enableArrays(getAttributeCount());
		format.vertexAttribPointers(0);

		glDrawArrays(GL_QUADS, 0, vertexCount);

		AttribUtil.disableArrays(getAttributeCount());

		vbo.unbind();
	}

	public void delete() {
		if (deleted) return;

		deleted = true;
		vbo.delete();
	}

	public int getAttributeCount() {
		return format.getAttributeCount();
	}

}

