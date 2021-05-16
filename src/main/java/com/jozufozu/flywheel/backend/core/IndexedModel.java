package com.jozufozu.flywheel.backend.core;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.backend.gl.GlPrimitiveType;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;

public class IndexedModel extends BufferedModel {

	protected GlPrimitiveType eboIndexType;
	protected GlBuffer ebo;

	public IndexedModel(VertexFormat modelFormat, ByteBuffer buf, int vertices) {
		super(modelFormat, buf, vertices);
	}

	@Override
	protected void init() {
		super.init();

		createEBO();
	}

	@Override
    protected void doRender() {
        modelVBO.bind();
        ebo.bind();

        setupAttributes();
        GL20.glDrawElements(GL20.GL_QUADS, vertexCount, eboIndexType.getGlConstant(), 0);

        int numAttributes = getTotalShaderAttributeCount();
        for (int i = 0; i <= numAttributes; i++) {
            GL20.glDisableVertexAttribArray(i);
        }

        ebo.unbind();
        modelVBO.unbind();
    }

    protected final void createEBO() {
		ebo = new GlBuffer(GlBufferType.ELEMENT_ARRAY_BUFFER);
		eboIndexType = GlPrimitiveType.UINT; // TODO: choose this based on the number of vertices

		int indicesSize = vertexCount * eboIndexType.getSize();

		ebo.bind();

		ebo.alloc(indicesSize);
		MappedBuffer indices = ebo.getBuffer(0, indicesSize);
		for (int i = 0; i < vertexCount; i++) {
			indices.putInt(i);
		}
		indices.flush();

		ebo.unbind();
	}

    @Override
    protected void deleteInternal() {
        super.deleteInternal();
        ebo.delete();
    }
}
