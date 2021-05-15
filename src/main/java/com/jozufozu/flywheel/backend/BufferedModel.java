package com.jozufozu.flywheel.backend;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;
import com.simibubi.create.foundation.render.TemplateBuffer;

import net.minecraft.client.renderer.BufferBuilder;

public abstract class BufferedModel extends TemplateBuffer {

	protected final VertexFormat modelFormat;
	protected GlBuffer modelVBO;
	private boolean initialized; // lazy init
	private boolean removed;

	protected BufferedModel(VertexFormat modelFormat, BufferBuilder buf) {
		super(buf);
		this.modelFormat = modelFormat;
	}

	/**
	 * Renders this model, checking first if there is anything to render.
	 */
	public final void render() {
		if (vertexCount == 0 || removed) return;

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
		int stride = modelFormat.getStride();
		int invariantSize = vertexCount * stride;

		// allocate the buffer on the gpu
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, invariantSize, GL15.GL_STATIC_DRAW);

		// mirror it in system memory so we can write to it
		MappedBuffer buffer = modelVBO.getBuffer(0, invariantSize);
		for (int i = 0; i < vertexCount; i++) {
			copyVertex(buffer, i);
		}
		buffer.flush();
	}

	protected abstract void copyVertex(MappedBuffer to, int index);

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
