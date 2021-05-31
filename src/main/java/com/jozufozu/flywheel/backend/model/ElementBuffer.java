package com.jozufozu.flywheel.backend.model;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.backend.gl.GlNumericType;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;

public class ElementBuffer extends GlBuffer {

	public final int elementCount;
	public final GlNumericType eboIndexType;

	public ElementBuffer(ByteBuffer indices, int elementCount, GlNumericType indexType) {
		super(GlBufferType.ELEMENT_ARRAY_BUFFER);
		this.eboIndexType = indexType;
		this.elementCount = elementCount;

		int indicesSize = elementCount * indexType.getByteWidth();

		bind();

		alloc(indicesSize);
		getBuffer(0, indicesSize)
				.put(indices)
				.flush();

		unbind();
	}
}
