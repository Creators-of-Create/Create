package com.simibubi.create.content.logistics.flwdata;

import com.jozufozu.flywheel.api.instance.InstanceHandle;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.InstanceWriter;
import com.jozufozu.flywheel.api.layout.Layout;
import com.jozufozu.flywheel.lib.layout.BufferLayout;
import com.simibubi.create.foundation.render.AllInstanceLayouts;
import com.simibubi.create.foundation.render.AllInstanceShaders;

import net.minecraft.resources.ResourceLocation;

public class FlapType implements InstanceType<FlapData> {
	@Override
	public FlapData create(InstanceHandle instanceHandle) {
		return new FlapData(this, instanceHandle);
	}

	@Override
	public Layout layout() {
		return null;
	}

	@Override
	public InstanceWriter<FlapData> getWriter() {
		return FlapWriter.INSTANCE;
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
		return AllInstanceLayouts.FLAP;
	}

	@Override
	public ResourceLocation getProgramSpec() {
		return AllInstanceShaders.FLAPS;
	}


	private static float getFlapAngle(float flapness, float intensity, float scale) {
		float absFlap = Math.abs(flapness);

		float angle = (float) (Math.sin((1. - absFlap) * Math.PI * intensity) * 30. * flapness * scale);

		if (flapness > 0) {
			return angle * 0.5f;
		} else {
			return angle;
		}
	}
}
