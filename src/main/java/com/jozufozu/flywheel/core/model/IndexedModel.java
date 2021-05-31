package com.jozufozu.flywheel.core.model;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlPrimitive;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;

public class IndexedModel extends BufferedModel {

	protected ElementBuffer ebo;

	public IndexedModel(GlPrimitive primitiveMode, VertexFormat modelFormat, ByteBuffer buf, int vertices, ElementBuffer ebo) {
		super(primitiveMode, modelFormat, buf, vertices);

		this.ebo = ebo;
	}

	@Override
	public void setupState() {
		super.setupState();
		ebo.bind();
	}

	@Override
	public void clearState() {
		super.clearState();
		ebo.unbind();
	}

	@Override
	public void drawCall() {
		GL20.glDrawElements(primitiveMode.glEnum, ebo.elementCount, ebo.eboIndexType.getGlEnum(), 0);
	}

	@Override
	public void drawInstances(int instanceCount) {
		if (vertexCount <= 0 || deleted) return;

		Backend.compat.drawInstanced.drawElementsInstanced(primitiveMode, ebo.elementCount, ebo.eboIndexType, 0, instanceCount);
	}

	@Override
	public void delete() {
		super.delete();
		ebo.delete();
	}
}
