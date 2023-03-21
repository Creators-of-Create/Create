package com.simibubi.create.content.logistics.trains;

import com.jozufozu.flywheel.util.transform.Transform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.nbt.CompoundTag;

import org.jetbrains.annotations.Nullable;

public class StandardBogeyRenderer extends BogeyRenderer {

	public StandardBogeyRenderer() {
		renderers.put(BogeySize.SMALL, this::renderSmall);
		renderers.put(BogeySize.LARGE, this::renderLarge);
	}

	public void renderSmall(CompoundTag bogeyData, float wheelAngle, PoseStack ms, int light,
							@Nullable VertexConsumer vb) {
		boolean inContraption = vb != null;
		Transform<?> transform = getTransformFromPartial(AllBlockPartials.BOGEY_FRAME, inContraption);
		finalize(transform, ms, light, vb);

		Transform<?>[] wheels = getTransformsFromPartial(AllBlockPartials.SMALL_BOGEY_WHEELS, inContraption, 2);
		for (int side : Iterate.positiveAndNegative) {
			if (!inContraption)
				ms.pushPose();
			Transform<?> wheel = wheels[(side + 1)/2];
			wheel.translate(0, 12 / 16f, side)
					.rotateX(wheelAngle);
			finalize(wheel, ms, light, vb);
			if (!inContraption)
				ms.popPose();
		}
	}

	public  void renderLarge(CompoundTag bogeyData, float wheelAngle, PoseStack ms, int light,
							 @Nullable VertexConsumer vb) {
		boolean inContraption = vb != null;

		Transform<?> bogeyDrive = getTransformFromPartial(AllBlockPartials.BOGEY_DRIVE, inContraption);
		finalize(bogeyDrive, ms, light, vb);

		Transform<?> bogeyPiston = getTransformFromPartial(AllBlockPartials.BOGEY_PISTON, inContraption)
				.translate(0, 0, 1 / 4f * Math.sin(AngleHelper.rad(wheelAngle)));
		finalize(bogeyPiston, ms, light, vb);

		if (!inContraption)
			ms.pushPose();

		Transform<?> bogeyWheels = getTransformFromPartial(AllBlockPartials.LARGE_BOGEY_WHEELS, inContraption)
				.translate(0, 1, 0)
				.rotateX(wheelAngle);
		finalize(bogeyWheels, ms, light, vb);

		Transform<?> bogeyPin = getTransformFromPartial(AllBlockPartials.BOGEY_PIN, inContraption)
				.translate(0, 1, 0)
				.rotateX(wheelAngle)
				.translate(0, 1 / 4f, 0)
				.rotateX(-wheelAngle);
		finalize(bogeyPin, ms, light, vb);
		
		if (!inContraption)
			ms.popPose();
	}
}
