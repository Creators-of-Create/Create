package com.simibubi.create.content.logistics.packagePort.frogport;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import com.simibubi.create.AllPartialModels;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class FrogportVisual extends AbstractBlockEntityVisual<FrogportBlockEntity> implements SimpleDynamicVisual {
	private final TransformedInstance body;
	private TransformedInstance head;
	private final TransformedInstance tongue;
	private final TransformedInstance rig;
	private final TransformedInstance box;

	private final Matrix4f basePose = new Matrix4f();
	private float lastYaw = Float.NaN;
	private float lastHeadPitch = Float.NaN;
	private float lastTonguePitch = Float.NaN;
	private float lastTongueLength = Float.NaN;
	private boolean lastGoggles = false;

	public FrogportVisual(VisualizationContext ctx, FrogportBlockEntity blockEntity, float partialTick) {
		super(ctx, blockEntity, partialTick);

		body = ctx.instancerProvider()
			.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.FROGPORT_BODY))
			.createInstance();

		head = ctx.instancerProvider()
			.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.FROGPORT_HEAD))
			.createInstance();

		tongue = ctx.instancerProvider()
			.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.FROGPORT_TONGUE))
			.createInstance();

		rig = ctx.instancerProvider()
			.instancer(InstanceTypes.TRANSFORMED, Models.block(Blocks.AIR.defaultBlockState()))
			.createInstance();

		box = ctx.instancerProvider()
			.instancer(InstanceTypes.TRANSFORMED, Models.block(Blocks.AIR.defaultBlockState()))
			.createInstance();

		rig.handle().setVisible(false);
		box.handle().setVisible(false);

		animate(partialTick);
	}

	@Override
	public void beginFrame(Context ctx) {
		animate(ctx.partialTick());
	}

	private void animate(float partialTicks) {
		updateGoggles();
		
		float yaw = blockEntity.getYaw();

		float headPitch = 80;
		float tonguePitch = 0;
		float tongueLength = 0;
		float headPitchModifier = 1;

		boolean hasTarget = blockEntity.target != null;
		boolean animating = blockEntity.isAnimationInProgress();
		boolean depositing = blockEntity.currentlyDepositing;

		Vec3 diff = Vec3.ZERO;

		if (hasTarget) {
			diff = blockEntity.target
				.getExactTargetLocation(blockEntity, blockEntity.getLevel(), blockEntity.getBlockPos())
				.subtract(0, animating && depositing ? 0 : 0.75, 0)
				.subtract(Vec3.atCenterOf(blockEntity.getBlockPos()));
			tonguePitch = (float) Mth.atan2(diff.y, diff.multiply(1, 0, 1)
				.length() + (3 / 16f)) * Mth.RAD_TO_DEG;
			tongueLength = Math.max((float) diff.length(), 1);
			headPitch = Mth.clamp(tonguePitch * 2, 60, 100);
		}

		if (animating) {
			float progress = blockEntity.animationProgress.getValue(partialTicks);
			float scale = 1;
			float itemDistance = 0;

			if (depositing) {
				double modifier = Math.max(0, 1 - Math.pow((progress - 0.25) * 4 - 1, 4));
				itemDistance =
					(float) Math.max(tongueLength * Math.min(1, (progress - 0.25) * 3), tongueLength * modifier);
				tongueLength *= Math.max(0, 1 - Math.pow((progress * 1.25 - 0.25) * 4 - 1, 4));
				headPitchModifier = (float) Math.max(0, 1 - Math.pow((progress * 1.25) * 2 - 1, 4));
				scale = 0.25f + progress * 3 / 4;

			} else {
				tongueLength *= Math.pow(Math.max(0, 1 - progress * 1.25), 5);
				headPitchModifier = 1 - (float) Math.min(1, Math.max(0, (Math.pow(progress * 1.5, 2) - 0.5) * 2));
				scale = (float) Math.max(0.5, 1 - progress * 1.25);
				itemDistance = tongueLength;
			}

			renderPackage(diff, scale, itemDistance);

		} else {
			tongueLength = 0;
			float anticipation = blockEntity.anticipationProgress.getValue(partialTicks);
			headPitchModifier =
				anticipation > 0 ? (float) Math.max(0, 1 - Math.pow((anticipation * 1.25) * 2 - 1, 4)) : 0;
			rig.handle()
				.setVisible(false);
			box.handle()
				.setVisible(false);
		}

		headPitch *= headPitchModifier;

		headPitch = Math.max(headPitch, blockEntity.manualOpenAnimationProgress.getValue(partialTicks) * 60);
		tongueLength = Math.max(tongueLength, blockEntity.manualOpenAnimationProgress.getValue(partialTicks) * 0.25f);
if (yaw != lastYaw) {
			body.setIdentityTransform()
				.translate(getVisualPosition())
				.center()
				.rotateYDegrees(yaw)
				.uncenter()
				.setChanged();

			// Save the base pose to avoid recalculating it twice every frame
			basePose.set(body.pose)
				.translate(8 / 16f, 10 / 16f, 11 / 16f);

			// I'm not entirely sure that yaw ever changes
			lastYaw = yaw;

			// Force the head and tongue to update
			lastTonguePitch = Float.NaN;
			lastHeadPitch = Float.NaN;
		}

		if (headPitch != lastHeadPitch) {
			head.setTransform(basePose)
				.rotateXDegrees(headPitch)
				.translateBack(8 / 16f, 10 / 16f, 11 / 16f)
				.setChanged();

			lastHeadPitch = headPitch;
		}

		if (tonguePitch != lastTonguePitch || tongueLength != lastTongueLength) {
			tongue.setTransform(basePose)
				.rotateXDegrees(tonguePitch)
				.scale(1f, 1f, tongueLength / (7 / 16f))
				.translateBack(8 / 16f, 10 / 16f, 11 / 16f)
				.setChanged();

			lastTonguePitch = tonguePitch;
			lastTongueLength = tongueLength;
		}
	}

	public void updateGoggles() {
		if (blockEntity.goggles && !lastGoggles) {
			head.delete();
			head = instancerProvider()
				.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.FROGPORT_HEAD_GOGGLES))
				.createInstance();
			lastHeadPitch = -1;
			updateLight(0);
			lastGoggles = true;
		}
		
		if (!blockEntity.goggles && lastGoggles) {
			head.delete();
			head = instancerProvider()
				.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.FROGPORT_HEAD))
				.createInstance();
			lastHeadPitch = -1;
			updateLight(0);
			lastGoggles = false;
		}
	}

	private void renderPackage(Vec3 diff, float scale, float itemDistance) {
		if (blockEntity.animatedPackage == null || scale < 0.45) {
			rig.handle()
				.setVisible(false);
			box.handle()
				.setVisible(false);
			return;
		}
		ResourceLocation key = BuiltInRegistries.ITEM.getKey(blockEntity.animatedPackage.getItem());
		if (key == BuiltInRegistries.ITEM.getDefaultKey()) {
			rig.handle()
				.setVisible(false);
			box.handle()
				.setVisible(false);
			return;
		}

		boolean animating = blockEntity.isAnimationInProgress();
		boolean depositing = blockEntity.currentlyDepositing;

		instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.PACKAGES.get(key)))
			.stealInstance(box);
		box.handle().setVisible(true);

		box.setIdentityTransform()
			.translate(getVisualPosition())
			.translate(0, 3 / 16f, 0)
			.translate(diff.normalize()
				.scale(itemDistance)
				.subtract(0, animating && depositing ? 0.75 : 0, 0))
			.center()
			.scale(scale)
			.uncenter()
			.setChanged();

		if (!depositing) {
			rig.handle()
				.setVisible(false);
			return;
		}

		instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.PACKAGE_RIGGING.get(key)))
			.stealInstance(rig);
		rig.handle().setVisible(true);

		rig.pose.set(box.pose);
		rig.setChanged();
	}

	@Override
	public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
		consumer.accept(body);
		consumer.accept(head);
	}

	@Override
	public void updateLight(float partialTick) {
		relight(body, head, tongue, rig, box);
	}

	@Override
	protected void _delete() {
		body.delete();
		head.delete();
		tongue.delete();
		rig.delete();
		box.delete();
	}
}
