package com.simibubi.create.content.trains.bogey;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.content.processing.burner.ScrollTransformedInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.render.SpecialModels;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

public class StandardBogeyVisual implements BogeyVisual {
	private final TransformedInstance shaft1;
	private final TransformedInstance shaft2;

	public StandardBogeyVisual(VisualizationContext ctx, float partialTick, boolean inContraption) {
		var shaftInstancer = ctx.instancerProvider()
			.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.SHAFT));

		shaft1 = shaftInstancer.createInstance();
		shaft2 = shaftInstancer.createInstance();
	}

	@Override
	public void update(CompoundTag bogeyData, float wheelAngle, PoseStack poseStack) {
		shaft1.setTransform(poseStack)
			.translate(-.5f, .25f, 0)
			.center()
			.rotateTo(Direction.UP, Direction.SOUTH)
			.rotateYDegrees(wheelAngle)
			.uncenter()
			.setChanged();
		shaft2.setTransform(poseStack)
			.translate(-.5f, .25f, -1)
			.center()
			.rotateTo(Direction.UP, Direction.SOUTH)
			.rotateYDegrees(wheelAngle)
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
	public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
		consumer.accept(shaft1);
		consumer.accept(shaft2);
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

		public Small(VisualizationContext ctx, float partialTick, boolean inContraption) {
			super(ctx, partialTick, inContraption);
			var wheelInstancer = ctx.instancerProvider()
				.instancer(InstanceTypes.TRANSFORMED, SpecialModels.smoothLit(AllPartialModels.SMALL_BOGEY_WHEELS));
			frame = ctx.instancerProvider()
				.instancer(InstanceTypes.TRANSFORMED, SpecialModels.smoothLit(AllPartialModels.BOGEY_FRAME))
					.createInstance();
			wheel1 = wheelInstancer.createInstance();
			wheel2 = wheelInstancer.createInstance();
		}

		@Override
		public void update(CompoundTag bogeyData, float wheelAngle, PoseStack poseStack) {
			super.update(bogeyData, wheelAngle, poseStack);
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
		public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
			super.collectCrumblingInstances(consumer);
			consumer.accept(frame);
			consumer.accept(wheel1);
			consumer.accept(wheel2);
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
		private final ScrollTransformedInstance belt;
		private final TransformedInstance piston;
		private final TransformedInstance wheels;
		private final TransformedInstance pin;

		public Large(VisualizationContext ctx, float partialTick, boolean inContraption) {
			super(ctx, partialTick, inContraption);
			var secondaryShaftInstancer = ctx.instancerProvider()
				.instancer(InstanceTypes.TRANSFORMED, SpecialModels.smoothLit(AllPartialModels.SHAFT));
			secondaryShaft1 = secondaryShaftInstancer.createInstance();
			secondaryShaft2 = secondaryShaftInstancer.createInstance();
			drive = ctx.instancerProvider()
				.instancer(InstanceTypes.TRANSFORMED, SpecialModels.smoothLit(AllPartialModels.BOGEY_DRIVE))
					.createInstance();
			belt = ctx.instancerProvider()
				.instancer(AllInstanceTypes.SCROLLING_TRANSFORMED, SpecialModels.smoothLit(AllPartialModels.BOGEY_DRIVE_BELT))
					.createInstance();
			piston = ctx.instancerProvider()
				.instancer(InstanceTypes.TRANSFORMED, SpecialModels.smoothLit(AllPartialModels.BOGEY_PISTON))
					.createInstance();
			wheels = ctx.instancerProvider()
				.instancer(InstanceTypes.TRANSFORMED, SpecialModels.smoothLit(AllPartialModels.LARGE_BOGEY_WHEELS))
					.createInstance();
			pin = ctx.instancerProvider()
				.instancer(InstanceTypes.TRANSFORMED, SpecialModels.smoothLit(AllPartialModels.BOGEY_PIN))
					.createInstance();

			belt.setSpriteShift(AllSpriteShifts.BOGEY_BELT);
		}

		@Override
		public void update(CompoundTag bogeyData, float wheelAngle, PoseStack poseStack) {
			super.update(bogeyData, wheelAngle, poseStack);
			secondaryShaft1.setTransform(poseStack)
				.translate(-.5f, .25f, .5f)
				.center()
				.rotateTo(Direction.UP, Direction.EAST)
				.rotateYDegrees(wheelAngle)
				.uncenter()
				.setChanged();
			secondaryShaft2.setTransform(poseStack)
				.translate(-.5f, .25f, -1.5f)
				.center()
				.rotateTo(Direction.UP, Direction.EAST)
				.rotateYDegrees(wheelAngle)
				.uncenter()
				.setChanged();
			drive.setTransform(poseStack)
				.scale(1 - 1/512f)
				.setChanged();
			belt.offset(0, StandardBogeyRenderer.Large.BELT_RADIUS_IN_UV_SPACE * Mth.DEG_TO_RAD * wheelAngle)
				.setTransform(poseStack)
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
			belt.setZeroTransform().setChanged();
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
			belt.light(packedLight).setChanged();
			piston.light(packedLight).setChanged();
			pin.light(packedLight).setChanged();
		}

		@Override
		public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
			super.collectCrumblingInstances(consumer);
			consumer.accept(secondaryShaft1);
			consumer.accept(secondaryShaft2);
			consumer.accept(wheels);
			consumer.accept(drive);
			consumer.accept(belt);
			consumer.accept(piston);
			consumer.accept(pin);
		}

		@Override
		public void delete() {
			super.delete();
			secondaryShaft1.delete();
			secondaryShaft2.delete();
			wheels.delete();
			drive.delete();
			belt.delete();
			piston.delete();
			pin.delete();
		}
	}
}
