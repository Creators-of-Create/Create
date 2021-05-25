package com.jozufozu.flywheel.backend.core;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.backend.gl.GlPrimitiveType;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.util.AttribUtil;

public class IndexedModel extends BufferedModel {

	protected GlPrimitiveType eboIndexType;
	protected GlBuffer ebo;

	public IndexedModel(VertexFormat modelFormat, ByteBuffer buf, int vertices, ByteBuffer indices, GlPrimitiveType indexType) {
		super(modelFormat, buf, vertices);

		ebo = new GlBuffer(GlBufferType.ELEMENT_ARRAY_BUFFER);
		this.eboIndexType = indexType;

		int indicesSize = vertexCount * indexType.getSize();

		ebo.bind();

		ebo.alloc(indicesSize);
		ebo.getBuffer(0, indicesSize)
				.put(indices)
				.flush();

		ebo.unbind();
	}

	public void render() {
		vbo.bind();
		ebo.bind();

		AttribUtil.enableArrays(getAttributeCount());
		format.vertexAttribPointers(0);

		GL20.glDrawElements(GL20.GL_QUADS, vertexCount, eboIndexType.getGlConstant(), 0);

		AttribUtil.disableArrays(getAttributeCount());

		ebo.unbind();
		vbo.unbind();
	}

	@Override
	public void delete() {
		super.delete();
		ebo.delete();
	}
}
