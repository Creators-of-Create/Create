package com.simibubi.create.content.logistics.trains;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.util.transform.Transform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import org.jetbrains.annotations.Nullable;

import static com.simibubi.create.AllBlockPartials.LARGE_BOGEY_WHEELS;
import static com.simibubi.create.AllBlockPartials.BOGEY_PIN;
import static com.simibubi.create.AllBlockPartials.BOGEY_DRIVE;
import static com.simibubi.create.AllBlockPartials.BOGEY_PISTON;
import static com.simibubi.create.AllBlockPartials.SMALL_BOGEY_WHEELS;
import static com.simibubi.create.AllBlockPartials.BOGEY_FRAME;

public class StandardBogeyRenderer {
	public static class CommonStandardBogeyRenderer extends BogeyRenderer.CommonRenderer {
		@Override
		public void initialiseContraptionModelData(MaterialManager materialManager) {
			createModelInstances(materialManager, AllBlocks.SHAFT.getDefaultState()
					.setValue(ShaftBlock.AXIS, Direction.Axis.Z), 2);
		}

		@Override
		public void render(CompoundTag bogeyData, float wheelAngle, PoseStack ms, int light, VertexConsumer vb) {
			boolean inContraption = vb == null;
			Transform<?>[] shafts = getTransformsFromBlockState(AllBlocks.SHAFT.getDefaultState()
					.setValue(ShaftBlock.AXIS, Direction.Axis.Z), ms, inContraption, 2);
			for (int i : Iterate.zeroAndOne) {
				shafts[i].translate(-.5f, .25f, i * -1)
						.centre()
						.rotateZ(wheelAngle)
						.unCentre();
				finalize(shafts[i], ms, light, vb);
			}
		}
	}


	public static class SmallStandardBogeyRenderer extends BogeyRenderer {
		@Override
		public void initialiseContraptionModelData(MaterialManager materialManager) {
			createModelInstances(materialManager, SMALL_BOGEY_WHEELS, 2);
			createModelInstances(materialManager, BOGEY_FRAME);
		}


		@Override
		public BogeySizes.BogeySize getSize() {
			return BogeySizes.SMALL;
		}

		@Override
		public void render(CompoundTag bogeyData, float wheelAngle, PoseStack ms, int light, VertexConsumer vb) {
			boolean inContraption = vb == null;
			Transform<?> transform = getTransformFromPartial(BOGEY_FRAME, ms, inContraption);
			finalize(transform, ms, light, vb);

			Transform<?>[] wheels = getTransformsFromPartial(SMALL_BOGEY_WHEELS, ms, inContraption, 2);
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
	}

	public static class LargeStandardBogeyRenderer extends BogeyRenderer {
		@Override
		public void initialiseContraptionModelData(MaterialManager materialManager) {
			createModelInstances(materialManager, LARGE_BOGEY_WHEELS, BOGEY_DRIVE, BOGEY_PISTON, BOGEY_PIN);
			createModelInstances(materialManager, AllBlocks.SHAFT.getDefaultState()
					.setValue(ShaftBlock.AXIS, Direction.Axis.X), 2);
		}

		@Override
		public BogeySizes.BogeySize getSize() {
			return BogeySizes.LARGE;
		}

		@Override
		public void render(CompoundTag bogeyData, float wheelAngle, PoseStack ms, int light, VertexConsumer vb) {
			boolean inContraption = vb == null;

			Transform<?>[] secondaryShafts = getTransformsFromBlockState(AllBlocks.SHAFT.getDefaultState()
					.setValue(ShaftBlock.AXIS, Direction.Axis.X), ms, inContraption, 2);

			for (int i : Iterate.zeroAndOne) {
				Transform<?> secondShaft = secondaryShafts[i];
				secondShaft.translate(-.5f, .25f, .5f + i * -2)
						.centre()
						.rotateX(wheelAngle)
						.unCentre();
				finalize(secondShaft, ms, light, vb);
			}

			Transform<?> bogeyDrive = getTransformFromPartial(BOGEY_DRIVE, ms, inContraption);
			finalize(bogeyDrive, ms, light, vb);

			Transform<?> bogeyPiston = getTransformFromPartial(BOGEY_PISTON, ms, inContraption)
					.translate(0, 0, 1 / 4f * Math.sin(AngleHelper.rad(wheelAngle)));
			finalize(bogeyPiston, ms, light, vb);

			if (!inContraption)
				ms.pushPose();

			Transform<?> bogeyWheels = getTransformFromPartial(LARGE_BOGEY_WHEELS, ms, inContraption)
					.translate(0, 1, 0)
					.rotateX(wheelAngle);
			finalize(bogeyWheels, ms, light, vb);

			Transform<?> bogeyPin = getTransformFromPartial(BOGEY_PIN, ms, inContraption)
					.translate(0, 1, 0)
					.rotateX(wheelAngle)
					.translate(0, 1 / 4f, 0)
					.rotateX(-wheelAngle);
			finalize(bogeyPin, ms, light, vb);

			if (!inContraption)
				ms.popPose();
		}
	}
}
