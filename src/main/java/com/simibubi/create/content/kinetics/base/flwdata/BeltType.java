package com.simibubi.create.content.kinetics.base.flwdata;

import org.joml.Quaternionf;

import com.jozufozu.flywheel.api.struct.Batched;
import com.jozufozu.flywheel.api.struct.Instanced;
import com.jozufozu.flywheel.api.struct.StructWriter;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.jozufozu.flywheel.core.model.ModelTransformer;
import com.jozufozu.flywheel.util.RenderMath;
import com.simibubi.create.content.kinetics.KineticDebugger;
import com.simibubi.create.foundation.render.AllInstanceFormats;
import com.simibubi.create.foundation.render.AllProgramSpecs;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.resources.ResourceLocation;

public class BeltType implements Instanced<BeltData>, Batched<BeltData> {
	@Override
	public BeltData create() {
		return new BeltData();
	}

	@Override
	public BufferLayout getLayout() {
		return AllInstanceFormats.BELT;
	}

	@Override
	public StructWriter<BeltData> getWriter(VecBuffer backing) {
		return new BeltWriterUnsafe(backing, this);
	}

	@Override
	public ResourceLocation getProgramSpec() {
		return AllProgramSpecs.BELT;
	}

	@Override
	public void transform(BeltData d, ModelTransformer.Params b) {
		float spriteHeight = d.maxV - d.minV;
		double scroll = d.rotationalSpeed * AnimationTickHolder.getRenderTime() / (31.5 * 16) + d.rotationOffset;
		scroll = scroll - Math.floor(scroll);
		scroll = scroll * spriteHeight * RenderMath.f(d.scrollMult);

		float finalScroll = (float) scroll;
		b.shiftUV((builder, u, v) -> {
			float targetU = u - d.sourceU + d.minU;
			float targetV = v - d.sourceV + d.minV
					+ finalScroll;
			builder.uv(targetU, targetV);
		});

		b.translate(d.x + 0.5, d.y + 0.5, d.z + 0.5)
				.multiply(new Quaternionf(d.qX, d.qY, d.qZ, d.qW))
				.unCentre()
				.light(d.getPackedLight());
		if (KineticDebugger.isActive()) {
			b.color(d.r, d.g, d.b, d.a);
		}
	}
}
