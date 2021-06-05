package com.jozufozu.flywheel.backend.model;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlPrimitive;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.core.QuadConverter;

/**
 * An indexed triangle model. Just what the driver ordered.
 *
 * <br><em>This should be favored over a normal BufferedModel.</em>
 */
public class IndexedModel extends BufferedModel {

	protected ElementBuffer ebo;

	public IndexedModel(VertexFormat modelFormat, ByteBuffer buf, int vertices, ElementBuffer ebo) {
		super(GlPrimitive.TRIANGLES, modelFormat, buf, vertices);

		this.ebo = ebo;
	}

	public static IndexedModel fromSequentialQuads(VertexFormat modelFormat, ByteBuffer quads, int vertices) {
		return new IndexedModel(modelFormat, quads, vertices, QuadConverter.getInstance().quads2Tris(vertices / 4));
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
	}
}
