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

	protected GlBuffer modelVBO;
	protected boolean removed;

	protected BufferedModel(BufferBuilder buf) {
		super(buf);
		if (vertexCount > 0) init();
	}

	protected void init() {

		modelVBO = new GlBuffer(GlBufferType.ARRAY_BUFFER);

		modelVBO.bind();
		initModel();
		modelVBO.unbind();
	}

	protected void initModel() {
		int stride = getModelFormat().getStride();
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

	protected abstract VertexFormat getModelFormat();

	protected int getTotalShaderAttributeCount() {
		return getModelFormat().getShaderAttributeCount();
	}

	/**
	 * Renders this model, checking first if there is anything to render.
	 */
	public final void render() {
		if (vertexCount == 0 || removed) return;

		doRender();
	}

	/**
	 * Set up any state and make the draw calls.
	 */
	protected abstract void doRender();

	protected void setupAttributes() {
		int numAttributes = getTotalShaderAttributeCount();
		for (int i = 0; i <= numAttributes; i++) {
			GL20.glEnableVertexAttribArray(i);
		}

		getModelFormat().vertexAttribPointers(0);
	}

	public final void delete() {
		removed = true;
		if (vertexCount > 0) {
			RenderWork.enqueue(this::deleteInternal);
		}
	}

	protected void deleteInternal() {
		modelVBO.delete();
	}
}
