package com.simibubi.create.content.contraptions.base.flwdata;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.backend.struct.StructType;
import com.jozufozu.flywheel.backend.struct.StructWriter;
import com.simibubi.create.foundation.render.AllInstanceFormats;

public class RotatingType implements StructType<RotatingData> {
	@Override
	public RotatingData create() {
		return new RotatingData();
	}

	@Override
	public VertexFormat format() {
		return AllInstanceFormats.ROTATING;
	}

	@Override
	public StructWriter<RotatingData> getWriter(VecBuffer backing) {
		return new UnsafeRotatingWriter(backing, this);
	}
}
