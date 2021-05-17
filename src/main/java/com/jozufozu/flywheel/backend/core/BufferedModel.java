package com.jozufozu.flywheel.backend.core;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.backend.RenderWork;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;

public abstract class BufferedModel {

	protected final ByteBuffer data;
	protected final VertexFormat modelFormat;
	protected final int vertexCount;
	protected GlBuffer modelVBO;
	private boolean initialized; // lazy init
	private boolean removed;

	protected BufferedModel(VertexFormat modelFormat, ByteBuffer data, int vertices) {
		this.data = data;
		this.modelFormat = modelFormat;
		this.vertexCount = vertices;
	}

	/**
	 * Renders this model, checking first if there is anything to render.
	 */
	public final void render() {
		if (vertexCount <= 0 || removed) return;

		if (!initialized) {
			// Lazily acquire resources in order to get around initialization order, as #getTotalShaderAttributeCount
			// might depend on fields in subclasses.
			init();
			initialized = true;
		}

		doRender();
	}

	/**
	 * Set up any state and make the draw calls.
	 */
	protected abstract void doRender();

	public final void delete() {
		removed = true;
		if (initialized) {
			RenderWork.enqueue(this::deleteInternal);
		}
	}

	protected void init() {
		modelVBO = new GlBuffer(GlBufferType.ARRAY_BUFFER);

		modelVBO.bind();
		initModel();
		modelVBO.unbind();
	}

	protected void initModel() {
		// allocate the buffer on the gpu
		modelVBO.alloc(data.capacity());

		// mirror it in system memory so we can write to it
		MappedBuffer buffer = modelVBO.getBuffer(0, data.capacity());
		buffer.put(data);
		buffer.flush();
	}

	protected int getTotalShaderAttributeCount() {
		return modelFormat.getShaderAttributeCount();
	}

	protected void setupAttributes() {
		int numAttributes = getTotalShaderAttributeCount();
		for (int i = 0; i <= numAttributes; i++) {
			GL20.glEnableVertexAttribArray(i);
		}

		modelFormat.vertexAttribPointers(0);
	}

	protected void deleteInternal() {
		modelVBO.delete();
	}
}
