package com.simibubi.create.content.contraptions.actors.flwdata;

import com.jozufozu.flywheel.api.instance.InstanceHandle;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.InstanceWriter;
import com.jozufozu.flywheel.api.layout.Layout;
import com.jozufozu.flywheel.lib.layout.BufferLayout;
import com.simibubi.create.foundation.render.AllInstanceLayouts;
import com.simibubi.create.foundation.render.AllInstanceShaders;

import net.minecraft.resources.ResourceLocation;

public class ActorType implements InstanceType<ActorInstance> {
	@Override
	public ActorInstance create(InstanceHandle instanceHandle) {
		return new ActorInstance(this, instanceHandle);
	}

	@Override
	public Layout layout() {
		return null;
	}

	@Override
	public InstanceWriter<ActorInstance> getWriter() {
		return UnsafeActorWriter.INSTANCE;
	}

	@Override
	public ResourceLocation vertexShader() {
		return null;
	}

	@Override
	public ResourceLocation cullShader() {
		return null;
	}

	@Override
	public BufferLayout getLayout() {
		return AllInstanceLayouts.ACTOR;
	}

	@Override
	public ResourceLocation getProgramSpec() {
		return AllInstanceShaders.ACTOR;
	}
}
