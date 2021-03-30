package com.simibubi.create.foundation.render.backend.core;

import com.simibubi.create.foundation.render.backend.gl.attrib.CommonAttributes;
import com.simibubi.create.foundation.render.backend.gl.attrib.IAttribSpec;
import com.simibubi.create.foundation.render.backend.gl.attrib.IVertexAttrib;

public enum BasicAttributes implements IVertexAttrib {
	LIGHT("aLight", CommonAttributes.LIGHT),
	COLOR("aColor", CommonAttributes.RGBA),
	;

	private final String name;
	private final IAttribSpec spec;

	BasicAttributes(String name, IAttribSpec spec) {
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
