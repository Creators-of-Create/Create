package com.simibubi.create.content.contraptions.base.flwdata;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.api.struct.Batched;
import com.jozufozu.flywheel.api.struct.BatchingTransformer;
import com.jozufozu.flywheel.api.struct.StructWriter;
import com.jozufozu.flywheel.api.struct.Instanced;
import com.jozufozu.flywheel.core.model.Model;
import com.simibubi.create.foundation.render.AllInstanceFormats;
import com.simibubi.create.foundation.render.AllProgramSpecs;

import net.minecraft.resources.ResourceLocation;

public class RotatingType implements Instanced<RotatingData>, Batched<RotatingData> {
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
	public ResourceLocation getProgramSpec() {
		return AllProgramSpecs.ROTATING;
	}

	@Override
	public BatchingTransformer<RotatingData> getTransformer(Model model) {
		return null;
	}
}
