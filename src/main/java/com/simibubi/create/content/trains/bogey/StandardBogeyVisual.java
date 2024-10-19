package com.simibubi.create.content.trains.bogey;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.trains.entity.CarriageBogey;
import com.simibubi.create.foundation.render.VirtualRenderHelper;
import com.simibubi.create.foundation.utility.AngleHelper;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import net.minecraft.core.Direction;

public class StandardBogeyVisual implements BogeyVisual {
	private final TransformedInstance shaft1;
	private final TransformedInstance shaft2;

	public StandardBogeyVisual(VisualizationContext ctx, CarriageBogey bogey, float partialTick) {
		var shaftInstancer = ctx.instancerProvider()
				.instancer(InstanceTypes.TRANSFORMED, VirtualRenderHelper.blockModel(AllBlocks.SHAFT.getDefaultState()
						.setValue(ShaftBlock.AXIS, Direction.Axis.Z)));
		shaft1 = shaftInstancer.createInstance();
		shaft2 = shaftInstancer.createInstance();
	}

	@Override
	public void update(float wheelAngle, PoseStack poseStack) {
		shaft1.setTransform(poseStack)
			.translate(-.5f, .25f, 0)
			.center()
			.rotateZDegrees(wheelAngle)
			.uncenter()
			.setChanged();
		shaft2.setTransform(poseStack)
			.translate(-.5f, .25f, -1)
			.center()
			.rotateZDegrees(wheelAngle)
			.uncenter()
			.setChanged();
	}

	@Override
	public void hide() {
		shaft1.setZeroTransform().setChanged();
		shaft2.setZeroTransform().setChanged();
	}

	@Override
	public void updateLight(int packedLight) {
		shaft1.light(packedLight).setChanged();
		shaft2.light(packedLight).setChanged();
	}

	@Override
	public void delete() {
		shaft1.delete();
		shaft2.delete();
	}

	public static class Small extends StandardBogeyVisual {
		private final TransformedInstance frame;
		private final TransformedInstance wheel1;
		private final TransformedInstance wheel2;

		public Small(VisualizationContext ctx, CarriageBogey bogey, float partialTick) {
			super(ctx, bogey, partialTick);
			var wheelInstancer = ctx.instancerProvider()
				.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.SMALL_BOGEY_WHEELS));
			frame = ctx.instancerProvider()
					.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.BOGEY_FRAME))
					.createInstance();
			wheel1 = wheelInstancer.createInstance();
			wheel2 = wheelInstancer.createInstance();
		}

		@Override
		public void update(float wheelAngle, PoseStack poseStack) {
			super.update(wheelAngle, poseStack);
			wheel1.setTransform(poseStack)
				.translate(0, 12 / 16f, -1)
				.rotateXDegrees(wheelAngle)
				.setChanged();
			wheel2.setTransform(poseStack)
				.translate(0, 12 / 16f, 1)
				.rotateXDegrees(wheelAngle)
				.setChanged();
			frame.setTransform(poseStack)
				.scale(1 - 1 / 512f)
				.setChanged();
		}

		@Override
		public void hide() {
			super.hide();
			frame.setZeroTransform().setChanged();
			wheel1.setZeroTransform().setChanged();
			wheel2.setZeroTransform().setChanged();
		}

		@Override
		public void updateLight(int packedLight) {
			super.updateLight(packedLight);
			frame.light(packedLight).setChanged();
			wheel1.light(packedLight).setChanged();
			wheel2.light(packedLight).setChanged();
		}

		@Override
		public void delete() {
			super.delete();
			frame.delete();
			wheel1.delete();
			wheel2.delete();
		}
	}

	public static class Large extends StandardBogeyVisual {
		private final TransformedInstance secondaryShaft1;
		private final TransformedInstance secondaryShaft2;
		private final TransformedInstance drive;
		private final TransformedInstance piston;
		private final TransformedInstance wheels;
		private final TransformedInstance pin;

		public Large(VisualizationContext ctx, CarriageBogey bogey, float partialTick) {
			super(ctx, bogey, partialTick);
			var secondaryShaftInstancer = ctx.instancerProvider()
					.instancer(InstanceTypes.TRANSFORMED, VirtualRenderHelper.blockModel(AllBlocks.SHAFT.getDefaultState()
							.setValue(ShaftBlock.AXIS, Direction.Axis.X)));
			secondaryShaft1 = secondaryShaftInstancer.createInstance();
			secondaryShaft2 = secondaryShaftInstancer.createInstance();
			drive = ctx.instancerProvider()
					.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.BOGEY_DRIVE))
					.createInstance();
			piston = ctx.instancerProvider()
					.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.BOGEY_PISTON))
					.createInstance();
			wheels = ctx.instancerProvider()
					.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.LARGE_BOGEY_WHEELS))
					.createInstance();
			pin = ctx.instancerProvider()
					.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.BOGEY_PIN))
					.createInstance();
		}

		@Override
		public void update(float wheelAngle, PoseStack poseStack) {
			super.update(wheelAngle, poseStack);
			secondaryShaft1.setTransform(poseStack)
				.translate(-.5f, .25f, .5f)
				.center()
				.rotateXDegrees(wheelAngle)
				.uncenter()
				.setChanged();
			secondaryShaft2.setTransform(poseStack)
				.translate(-.5f, .25f, -1.5f)
				.center()
				.rotateXDegrees(wheelAngle)
				.uncenter()
				.setChanged();
			drive.setTransform(poseStack)
				.scale(1 - 1/512f)
				.setChanged();
			piston.setTransform(poseStack)
				.translate(0, 0, 1 / 4f * Math.sin(AngleHelper.rad(wheelAngle)))
				.setChanged();
			wheels.setTransform(poseStack)
				.translate(0, 1, 0)
				.rotateXDegrees(wheelAngle)
				.setChanged();
			pin.setTransform(poseStack)
				.translate(0, 1, 0)
				.rotateXDegrees(wheelAngle)
				.translate(0, 1 / 4f, 0)
				.rotateXDegrees(-wheelAngle)
				.setChanged();
		}

		@Override
		public void hide() {
			super.hide();
			secondaryShaft1.setZeroTransform().setChanged();
			secondaryShaft2.setZeroTransform().setChanged();
			wheels.setZeroTransform().setChanged();
			drive.setZeroTransform().setChanged();
			piston.setZeroTransform().setChanged();
			pin.setZeroTransform().setChanged();
		}

		@Override
		public void updateLight(int packedLight) {
			super.updateLight(packedLight);
			secondaryShaft1.light(packedLight).setChanged();
			secondaryShaft2.light(packedLight).setChanged();
			wheels.light(packedLight).setChanged();
			drive.light(packedLight).setChanged();
			piston.light(packedLight).setChanged();
			pin.light(packedLight).setChanged();
		}

		@Override
		public void delete() {
			super.delete();
			secondaryShaft1.delete();
			secondaryShaft2.delete();
			wheels.delete();
			drive.delete();
			piston.delete();
			pin.delete();
		}
	}
}
