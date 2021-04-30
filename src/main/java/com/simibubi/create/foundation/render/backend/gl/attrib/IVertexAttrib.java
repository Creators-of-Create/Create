package com.simibubi.create.foundation.render.backend.gl.attrib;

public interface IVertexAttrib {

	String attribName();

	IAttribSpec attribSpec();

	int getDivisor();

	int getBufferIndex();
}
