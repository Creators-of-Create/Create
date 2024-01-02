package com.simibubi.create.content.kinetics.base.flwdata;

import com.jozufozu.flywheel.api.instance.InstanceHandle;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.InstanceWriter;
import com.jozufozu.flywheel.api.layout.Layout;
import com.jozufozu.flywheel.lib.layout.BufferLayout;
import com.simibubi.create.foundation.render.AllInstanceLayouts;
import com.simibubi.create.foundation.render.AllInstanceShaders;

import net.minecraft.resources.ResourceLocation;

public class BeltType implements InstanceType<BeltInstance> {
	@Override
	public BeltInstance create(InstanceHandle instanceHandle) {
		return new BeltInstance(this, instanceHandle);
	}

	@Override
	public Layout layout() {
		return null;
	}

	@Override
	public InstanceWriter<BeltInstance> getWriter() {
		return BeltWriter.INSTANCE;
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
	public BeltInstance create() {
		return new BeltInstance();
	}

	@Override
	public BufferLayout getLayout() {
		return AllInstanceLayouts.BELT;
	}

	@Override
	public ResourceLocation getProgramSpec() {
		return AllInstanceShaders.BELT;
	}
}
