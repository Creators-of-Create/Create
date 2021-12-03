package com.simibubi.create.content.logistics.block.flap;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.backend.struct.StructType;
import com.jozufozu.flywheel.backend.struct.StructWriter;
import com.simibubi.create.foundation.render.AllInstanceFormats;

public class FlapType implements StructType<FlapData> {
	@Override
	public FlapData create() {
		return new FlapData();
	}

	@Override
	public VertexFormat format() {
		return AllInstanceFormats.FLAP;
	}

	@Override
	public StructWriter<FlapData> getWriter(VecBuffer backing) {
		return new UnsafeFlapWriter(backing, this);
	}
}
