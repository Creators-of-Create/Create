package com.simibubi.create.content.logistics.flwdata;

import com.jozufozu.flywheel.api.struct.Batched;
import com.jozufozu.flywheel.api.struct.Instanced;
import com.jozufozu.flywheel.api.struct.StructWriter;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.jozufozu.flywheel.core.model.ModelTransformer;
import com.simibubi.create.foundation.render.AllInstanceFormats;
import com.simibubi.create.foundation.render.AllProgramSpecs;

import net.minecraft.resources.ResourceLocation;

public class FlapType implements Instanced<FlapData>, Batched<FlapData> {
	@Override
	public FlapData create() {
		return new FlapData();
	}

	@Override
	public BufferLayout getLayout() {
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
	public void transform(FlapData d, ModelTransformer.Params b) {
		b.translate(d.x, d.y, d.z)
				.centre()
				.rotateY(-d.horizontalAngle)
				.unCentre()
				.translate(d.pivotX, d.pivotY, d.pivotZ)
				.rotateX(getFlapAngle(d.flapness, d.intensity, d.flapScale))
				.translateBack(d.pivotX, d.pivotY, d.pivotZ)
				.translate(d.segmentOffsetX, d.segmentOffsetY, d.segmentOffsetZ)
				.light(d.getPackedLight());
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
