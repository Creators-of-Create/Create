package com.simibubi.create.content.contraptions.base.flwdata;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.backend.struct.Batched;
import com.jozufozu.flywheel.backend.struct.BatchingTransformer;
import com.jozufozu.flywheel.backend.struct.StructWriter;
import com.jozufozu.flywheel.backend.struct.Writeable;
import com.jozufozu.flywheel.core.model.Model;
import com.simibubi.create.foundation.render.AllInstanceFormats;

public class RotatingType implements Writeable<RotatingData>, Batched<RotatingData> {
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

	@Override
	public BatchingTransformer<RotatingData> getTransformer(Model model) {
		return null;
	}
}
