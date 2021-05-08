package com.jozufozu.flywheel.backend.core.materials;

import com.jozufozu.flywheel.backend.gl.attrib.CommonAttributes;
import com.jozufozu.flywheel.backend.gl.attrib.IAttribSpec;
import com.jozufozu.flywheel.backend.gl.attrib.IVertexAttrib;
import com.jozufozu.flywheel.backend.gl.attrib.VertexAttribSpec;

public enum ModelAttributes implements IVertexAttrib {
	VERTEX_POSITION("aPos", CommonAttributes.VEC3),
	NORMAL("aNormal", CommonAttributes.NORMAL),
	TEXTURE("aTexCoords", CommonAttributes.UV),
	;

	private final String name;
	private final VertexAttribSpec spec;

	ModelAttributes(String name, VertexAttribSpec spec) {
		this.name = name;
		this.spec = spec;
	}

	@Override
	public String attribName() {
		return name;
	}

	@Override
	public IAttribSpec attribSpec() {
		return spec;
	}

	@Override
	public int getDivisor() {
		return 0;
	}

	@Override
	public int getBufferIndex() {
		return 0;
	}
}
