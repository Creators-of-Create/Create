package com.simibubi.create.content.contraptions.components.actors.flwdata;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.backend.struct.StructType;
import com.jozufozu.flywheel.backend.struct.StructWriter;
import com.simibubi.create.foundation.render.AllInstanceFormats;

public class ActorType implements StructType<ActorData> {
	@Override
	public ActorData create() {
		return new ActorData();
	}

	@Override
	public VertexFormat format() {
		return AllInstanceFormats.BELT;
	}

	@Override
	public StructWriter<ActorData> getWriter(VecBuffer backing) {
		return new UnsafeActorWriter(backing, this);
	}
}
