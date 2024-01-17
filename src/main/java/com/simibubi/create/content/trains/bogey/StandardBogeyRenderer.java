package com.simibubi.create.content.trains.bogey;

import static com.simibubi.create.AllPartialModels.BOGEY_DRIVE;
import static com.simibubi.create.AllPartialModels.BOGEY_FRAME;
import static com.simibubi.create.AllPartialModels.BOGEY_PIN;
import static com.simibubi.create.AllPartialModels.BOGEY_PISTON;
import static com.simibubi.create.AllPartialModels.LARGE_BOGEY_WHEELS;
import static com.simibubi.create.AllPartialModels.SMALL_BOGEY_WHEELS;

import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.trains.entity.CarriageBogey;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public class StandardBogeyRenderer {
	public static class CommonStandardBogeyRenderer extends BogeyRenderer.CommonRenderer {
		@Override
		public void initialiseContraptionModelData(VisualizationContext context, CarriageBogey carriageBogey) {
			createModelInstance(context, AllBlocks.SHAFT.getDefaultState()
					.setValue(ShaftBlock.AXIS, Direction.Axis.Z), 2);
		}

		@Override
		public void render(CompoundTag bogeyData, float wheelAngle, PoseStack ms, int light, VertexConsumer vb, boolean inContraption) {
			boolean inInstancedContraption = vb == null;
			BogeyModelData[] shafts = getTransform(AllBlocks.SHAFT.getDefaultState()
					.setValue(ShaftBlock.AXIS, Direction.Axis.Z), ms, inInstancedContraption, 2);
			for (int i : Iterate.zeroAndOne) {
				shafts[i].translate(-.5f, .25f, i * -1)
						.center()
						.rotateZ(wheelAngle)
						.uncenter()
						.render(ms, light, vb);
			}
		}
	}


	public static class SmallStandardBogeyRenderer extends BogeyRenderer {
		@Override
		public void initialiseContraptionModelData(VisualizationContext context, CarriageBogey carriageBogey) {
			createModelInstance(context, SMALL_BOGEY_WHEELS, 2);
			createModelInstance(context, BOGEY_FRAME);
		}


		@Override
		public BogeySizes.BogeySize getSize() {
			return BogeySizes.SMALL;
		}

		@Override
		public void render(CompoundTag bogeyData, float wheelAngle, PoseStack ms, int light, VertexConsumer vb, boolean inContraption) {
			boolean inInstancedContraption = vb == null;
			getTransform(BOGEY_FRAME, ms, inInstancedContraption)
					.render(ms, light, vb);

			BogeyModelData[] wheels = getTransform(SMALL_BOGEY_WHEELS, ms, inInstancedContraption, 2);
			for (int side : Iterate.positiveAndNegative) {
				if (!inInstancedContraption)
					ms.pushPose();
				wheels[(side + 1)/2]
					.translate(0, 12 / 16f, side)
					.rotateX(wheelAngle)
					.render(ms, light, vb);
				if (!inInstancedContraption)
					ms.popPose();
			}
		}
	}

	public static class LargeStandardBogeyRenderer extends BogeyRenderer {
		@Override
		public void initialiseContraptionModelData(VisualizationContext context, CarriageBogey carriageBogey) {
			createModelInstance(context, LARGE_BOGEY_WHEELS, BOGEY_DRIVE, BOGEY_PISTON, BOGEY_PIN);
			createModelInstance(context, AllBlocks.SHAFT.getDefaultState()
					.setValue(ShaftBlock.AXIS, Direction.Axis.X), 2);
		}

		@Override
		public BogeySizes.BogeySize getSize() {
			return BogeySizes.LARGE;
		}

		@Override
		public void render(CompoundTag bogeyData, float wheelAngle, PoseStack ms, int light, VertexConsumer vb, boolean inContraption) {
			boolean inInstancedContraption = vb == null;

			BogeyModelData[] secondaryShafts = getTransform(AllBlocks.SHAFT.getDefaultState()
					.setValue(ShaftBlock.AXIS, Direction.Axis.X), ms, inInstancedContraption, 2);

			for (int i : Iterate.zeroAndOne) {
				secondaryShafts[i]
						.translate(-.5f, .25f, .5f + i * -2)
						.center()
						.rotateX(wheelAngle)
						.uncenter()
						.render(ms, light, vb);
			}

			getTransform(BOGEY_DRIVE, ms, inInstancedContraption)
					.render(ms, light, vb);

			getTransform(BOGEY_PISTON, ms, inInstancedContraption)
					.translate(0, 0, 1 / 4f * Math.sin(AngleHelper.rad(wheelAngle)))
					.render(ms, light, vb);

			if (!inInstancedContraption)
				ms.pushPose();

			getTransform(LARGE_BOGEY_WHEELS, ms, inInstancedContraption)
					.translate(0, 1, 0)
					.rotateX(wheelAngle)
					.render(ms, light, vb);

			getTransform(BOGEY_PIN, ms, inInstancedContraption)
					.translate(0, 1, 0)
					.rotateX(wheelAngle)
					.translate(0, 1 / 4f, 0)
					.rotateX(-wheelAngle)
					.render(ms, light, vb);

			if (!inInstancedContraption)
				ms.popPose();
		}
	}
}
