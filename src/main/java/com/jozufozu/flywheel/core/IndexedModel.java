package com.jozufozu.flywheel.core;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlNumericType;
import com.jozufozu.flywheel.backend.gl.GlPrimitive;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.util.AttribUtil;

public class IndexedModel extends BufferedModel {

	protected int elementCount;
	protected GlNumericType eboIndexType;
	protected GlBuffer ebo;

	public IndexedModel(GlPrimitive primitiveMode, VertexFormat modelFormat, ByteBuffer buf, int vertices, ByteBuffer indices, int elementCount, GlNumericType indexType) {
		super(primitiveMode, modelFormat, buf, vertices);

		this.ebo = new GlBuffer(GlBufferType.ELEMENT_ARRAY_BUFFER);
		this.eboIndexType = indexType;
		this.elementCount = elementCount;

		int indicesSize = elementCount * indexType.getSize();

		ebo.bind();

		ebo.alloc(indicesSize);
		ebo.getBuffer(0, indicesSize)
				.put(indices)
				.flush();

		ebo.unbind();
	}

	public void draw() {
		vbo.bind();
		ebo.bind();

		AttribUtil.enableArrays(getAttributeCount());
		format.vertexAttribPointers(0);

		GL20.glDrawElements(primitiveMode.glEnum, vertexCount, eboIndexType.getGlEnum(), 0);

		AttribUtil.disableArrays(getAttributeCount());

		ebo.unbind();
		vbo.unbind();
	}

	@Override
	public void drawInstances(int instanceCount) {
		if (vertexCount <= 0 || deleted) return;

		ebo.bind();
		Backend.compat.drawInstanced.drawElementsInstanced(primitiveMode, 0, eboIndexType, 0, instanceCount);
		ebo.unbind();
	}

	@Override
	public void delete() {
		super.delete();
		ebo.delete();
	}
}
