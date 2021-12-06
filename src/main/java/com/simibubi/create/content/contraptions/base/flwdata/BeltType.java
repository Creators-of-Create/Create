package com.simibubi.create.content.contraptions.base.flwdata;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.backend.struct.StructWriter;
import com.jozufozu.flywheel.backend.struct.Writeable;
import com.simibubi.create.foundation.render.AllInstanceFormats;

public class BeltType implements Writeable<BeltData> {
	@Override
	public BeltData create() {
		return new BeltData();
	}

	@Override
	public VertexFormat format() {
		return AllInstanceFormats.BELT;
	}

	@Override
	public StructWriter<BeltData> getWriter(VecBuffer backing) {
		return new UnsafeBeltWriter(backing, this);
	}
}
