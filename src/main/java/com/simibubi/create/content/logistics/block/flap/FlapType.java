package com.simibubi.create.content.logistics.block.flap;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.backend.struct.Batched;
import com.jozufozu.flywheel.backend.struct.BatchingTransformer;
import com.jozufozu.flywheel.backend.struct.StructWriter;
import com.jozufozu.flywheel.backend.struct.Writeable;
import com.jozufozu.flywheel.core.model.IModel;
import com.simibubi.create.foundation.render.AllInstanceFormats;

public class FlapType implements Writeable<FlapData>, Batched<FlapData> {
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

	@Override
	public BatchingTransformer<FlapData> getTransformer(IModel model) {
		return null;
	}
}
