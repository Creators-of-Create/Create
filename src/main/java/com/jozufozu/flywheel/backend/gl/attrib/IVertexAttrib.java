package com.jozufozu.flywheel.backend.gl.attrib;

public interface IVertexAttrib {

	String attribName();

	IAttribSpec attribSpec();

	int getDivisor();

	int getBufferIndex();
}
