package com.simibubi.create.content.contraptions.components.actors.flwdata;

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

public class ActorType implements Instanced<ActorData>, Batched<ActorData> {
	@Override
	public ActorData create() {
		return new ActorData();
	}

	@Override
	public VertexFormat format() {
		return AllInstanceFormats.ACTOR;
	}

	@Override
	public StructWriter<ActorData> getWriter(VecBuffer backing) {
		return new UnsafeActorWriter(backing, this);
	}

	@Override
	public ResourceLocation getProgramSpec() {
		return AllProgramSpecs.ACTOR;
	}

	@Override
	public BatchingTransformer<ActorData> getTransformer(Model model) {
		return null;
	}
}
