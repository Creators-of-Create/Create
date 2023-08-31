package com.simibubi.create.content.kinetics.base.flwdata;

import org.joml.Vector3f;

import com.jozufozu.flywheel.api.struct.Batched;
import com.jozufozu.flywheel.api.struct.Instanced;
import com.jozufozu.flywheel.api.struct.StructWriter;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.jozufozu.flywheel.core.model.ModelTransformer;
import com.jozufozu.flywheel.util.RenderMath;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.KineticDebugger;
import com.simibubi.create.foundation.render.AllInstanceFormats;
import com.simibubi.create.foundation.render.AllProgramSpecs;

import net.createmod.catnip.utility.AnimationTickHolder;
import net.minecraft.resources.ResourceLocation;

public class RotatingType implements Instanced<RotatingData>, Batched<RotatingData> {
	@Override
	public RotatingData create() {
		return new RotatingData();
	}

	@Override
	public BufferLayout getLayout() {
		return AllInstanceFormats.ROTATING;
	}

	@Override
	public StructWriter<RotatingData> getWriter(VecBuffer backing) {
		return new RotatingWriterUnsafe(backing, this);
	}

	@Override
	public ResourceLocation getProgramSpec() {
		return AllProgramSpecs.ROTATING;
	}

	@Override
	public void transform(RotatingData d, ModelTransformer.Params b) {
		float angle = ((AnimationTickHolder.getRenderTime() * d.rotationalSpeed * 3f / 10 + d.rotationOffset) % 360);

		Axis axis = Axis.of(new Vector3f(RenderMath.f(d.rotationAxisX), RenderMath.f(d.rotationAxisY), RenderMath.f(d.rotationAxisZ)));
		b.light(d.getPackedLight())
				.translate(d.x + 0.5, d.y + 0.5, d.z + 0.5)
				.multiply(axis.rotationDegrees(angle))
				.unCentre();

		if (KineticDebugger.isActive()) {
			b.color(d.r, d.g, d.b, d.a);
		}
	}
}
