package com.simibubi.create.foundation.render.backend.gl.attrib;

public interface IAttribSpec {

	void vertexAttribPointer(int stride, int index, int pointer);

	int getSize();

	int getAttributeCount();
}
