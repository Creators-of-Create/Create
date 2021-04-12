package com.simibubi.create.foundation.render.backend.effects;

import com.simibubi.create.foundation.render.backend.gl.attrib.CommonAttributes;
import com.simibubi.create.foundation.render.backend.gl.attrib.IAttribSpec;
import com.simibubi.create.foundation.render.backend.gl.attrib.IVertexAttrib;

public enum ScreenQuadAttributes implements IVertexAttrib {
	INSTANCE_POS("aVertex", CommonAttributes.VEC4),
	;

	private final String name;
	private final IAttribSpec spec;

	ScreenQuadAttributes(String name, IAttribSpec spec) {
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
