package com.simibubi.create.content.logistics.block.flap;

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

public class FlapType implements Instanced<FlapData>, Batched<FlapData> {
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
	public ResourceLocation getProgramSpec() {
		return AllProgramSpecs.FLAPS;
	}

	@Override
	public BatchingTransformer<FlapData> getTransformer(Model model) {
		return null;
	}
}
