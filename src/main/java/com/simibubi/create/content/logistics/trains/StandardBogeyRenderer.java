package com.simibubi.create.content.logistics.trains;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.util.transform.Transform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.nbt.CompoundTag;

import org.jetbrains.annotations.Nullable;

import static com.simibubi.create.AllBlockPartials.LARGE_BOGEY_WHEELS;
import static com.simibubi.create.AllBlockPartials.BOGEY_PIN;
import static com.simibubi.create.AllBlockPartials.BOGEY_DRIVE;
import static com.simibubi.create.AllBlockPartials.BOGEY_PISTON;
import static com.simibubi.create.AllBlockPartials.SMALL_BOGEY_WHEELS;
import static com.simibubi.create.AllBlockPartials.BOGEY_FRAME;

public class StandardBogeyRenderer extends BogeyRenderer {

	public StandardBogeyRenderer() {
		renderers.put(BogeySize.SMALL, this::renderSmall);
		renderers.put(BogeySize.LARGE, this::renderLarge);
	}

	@Override
	public void initialiseContraptionModelData(MaterialManager materialManager, BogeySize size) {
		// Large
		createModelInstances(materialManager, LARGE_BOGEY_WHEELS, BOGEY_DRIVE, BOGEY_PISTON, BOGEY_PIN);
		// Small
		createModelInstances(materialManager, SMALL_BOGEY_WHEELS, 2);
		createModelInstances(materialManager, BOGEY_FRAME);
	}

	public void renderSmall(CompoundTag bogeyData, float wheelAngle, PoseStack ms, int light,
							@Nullable VertexConsumer vb) {
		boolean inContraption = vb != null;
		Transform<?> transform = getTransformFromPartial(BOGEY_FRAME, inContraption);
		finalize(transform, ms, light, vb);

		Transform<?>[] wheels = getTransformsFromPartial(SMALL_BOGEY_WHEELS, inContraption, 2);
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

		Transform<?> bogeyDrive = getTransformFromPartial(BOGEY_DRIVE, inContraption);
		finalize(bogeyDrive, ms, light, vb);

		Transform<?> bogeyPiston = getTransformFromPartial(BOGEY_PISTON, inContraption)
				.translate(0, 0, 1 / 4f * Math.sin(AngleHelper.rad(wheelAngle)));
		finalize(bogeyPiston, ms, light, vb);

		if (!inContraption)
			ms.pushPose();

		Transform<?> bogeyWheels = getTransformFromPartial(LARGE_BOGEY_WHEELS, inContraption)
				.translate(0, 1, 0)
				.rotateX(wheelAngle);
		finalize(bogeyWheels, ms, light, vb);

		Transform<?> bogeyPin = getTransformFromPartial(BOGEY_PIN, inContraption)
				.translate(0, 1, 0)
				.rotateX(wheelAngle)
				.translate(0, 1 / 4f, 0)
				.rotateX(-wheelAngle);
		finalize(bogeyPin, ms, light, vb);
		ms.popPose();
	}
}
