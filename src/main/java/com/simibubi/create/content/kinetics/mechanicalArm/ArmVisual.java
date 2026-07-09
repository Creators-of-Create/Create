package com.simibubi.create.content.kinetics.mechanicalArm;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.google.common.collect.Lists;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.AbstractInstance;
import dev.engine_room.flywheel.lib.instance.FlatLit;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.engine_room.flywheel.lib.util.RecyclingPoseStack;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.data.Iterate;
import net.createmod.catnip.api.theme.Color;
import net.minecraft.client.Minecraft;
import com.simibubi.create.foundation.render.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class ArmVisual extends SingleAxisRotatingVisual<ArmBlockEntity> implements SimpleDynamicVisual {

	final TransformedInstance base;
	final TransformedInstance lowerBody;
	final TransformedInstance upperBody;
	final TransformedInstance claw;

	private final ArrayList<TransformedInstance> clawGrips;
	private final ArrayList<TransformedInstance> models;
	private final boolean ceiling;

	private final RecyclingPoseStack poseStack = new RecyclingPoseStack();

	private boolean wasDancing = false;
	private float baseAngle = Float.NaN;
	private float lowerArmAngle = Float.NaN;
	private float upperArmAngle = Float.NaN;
	private float headAngle = Float.NaN;

	public ArmVisual(VisualizationContext context, ArmBlockEntity blockEntity, float partialTick) {
		super(context, blockEntity, partialTick, Models.partial(AllPartialModels.ARM_COG));

		base = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ARM_BASE))
			.createInstance();
		lowerBody = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ARM_LOWER_BODY))
			.createInstance();
		upperBody = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ARM_UPPER_BODY))
			.createInstance();
		claw = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(blockEntity.goggles ? AllPartialModels.ARM_CLAW_BASE_GOGGLES : AllPartialModels.ARM_CLAW_BASE))
			.createInstance();

		TransformedInstance clawGrip1 = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ARM_CLAW_GRIP_UPPER))
			.createInstance();
		TransformedInstance clawGrip2 = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ARM_CLAW_GRIP_LOWER))
			.createInstance();

		clawGrips = Lists.newArrayList(clawGrip1, clawGrip2);
		models = Lists.newArrayList(base, lowerBody, upperBody, claw, clawGrip1, clawGrip2);
		ceiling = blockState.getValue(ArmBlock.CEILING);

		var msr = TransformStack.of(poseStack);
		msr.translate(getVisualPosition());
		msr.center();

		if (ceiling)
			msr.rotateXDegrees(180);

		animate(partialTick);
	}

	@Override
	public void beginFrame(DynamicVisual.Context ctx) {
		animate(ctx.partialTick());
	}

	private void animate(float pt) {
		if (blockEntity.phase == ArmBlockEntity.Phase.DANCING && blockEntity.getSpeed() != 0) {
			animateRave(pt);
			wasDancing = true;
			return;
		}

		float baseAngleNow = blockEntity.baseAngle.getValue(pt);
		float lowerArmAngleNow = blockEntity.lowerArmAngle.getValue(pt);
		float upperArmAngleNow = blockEntity.upperArmAngle.getValue(pt);
		float headAngleNow = blockEntity.headAngle.getValue(pt);

		boolean settled = Mth.equal(baseAngle, baseAngleNow) && Mth.equal(lowerArmAngle, lowerArmAngleNow)
			&& Mth.equal(upperArmAngle, upperArmAngleNow) && Mth.equal(headAngle, headAngleNow);

		this.baseAngle = baseAngleNow;
		this.lowerArmAngle = lowerArmAngleNow;
		this.upperArmAngle = upperArmAngleNow;
		this.headAngle = headAngleNow;

		// Need to reset the animation if the arm is dancing. We'd very likely be settled
		if (!settled || wasDancing)
			animateArm();

		wasDancing = false;
	}

	private void animateRave(float partialTick) {
		var ticks = AnimationTickHolder.getTicks();
		float renderTick = ticks + partialTick + (blockEntity.hashCode() % 64);

		float baseAngle = (renderTick * 10) % 360;
		float lowerArmAngle = Mth.lerp((Mth.sin(renderTick / 4) + 1) / 2, -45, 15);
		float upperArmAngle = Mth.lerp((Mth.sin(renderTick / 8) + 1) / 4, -45, 95);
		float headAngle = -lowerArmAngle;
		int color = Color.rainbowColor(ticks * 100)
			.getRGB();
		updateAngles(baseAngle, lowerArmAngle, upperArmAngle, headAngle, color);
	}

	private void animateArm() {
		updateAngles(this.baseAngle, this.lowerArmAngle - 135, this.upperArmAngle - 90, this.headAngle, 0xFFFFFF);
	}

	private void updateAngles(float baseAngle, float lowerArmAngle, float upperArmAngle, float headAngle, int color) {
		poseStack.pushPose();

		var msr = TransformStack.of(poseStack);

		ArmRenderer.transformBase(msr, baseAngle);
		base.setTransform(poseStack)
			.setChanged();

		ArmRenderer.transformLowerArm(msr, lowerArmAngle);
		lowerBody.setTransform(poseStack)
			.colorRgb(color)
			.setChanged();

		ArmRenderer.transformUpperArm(msr, upperArmAngle);
		upperBody.setTransform(poseStack)
			.colorRgb(color)
			.setChanged();

		ArmRenderer.transformHead(msr, headAngle);

		if (ceiling && blockEntity.goggles)
			msr.rotateZDegrees(180);

		claw.setTransform(poseStack)
			.setChanged();

		if (ceiling && blockEntity.goggles)
			msr.rotateZDegrees(180);

		ItemStack item = blockEntity.heldItem;
		ItemRenderer itemRenderer = com.simibubi.create.foundation.render.LegacyItemRendererBridge.getItemRenderer();
		boolean hasItem = !item.isEmpty();
		boolean isBlockItem = hasItem && (item.getItem() instanceof BlockItem)
			&& itemRenderer.getModel(item, Minecraft.getInstance().level, null, 0)
			.isGui3d();

		for (int index : Iterate.zeroAndOne) {
			poseStack.pushPose();
			int flip = index * 2 - 1;
			ArmRenderer.transformClawHalf(msr, hasItem, isBlockItem, flip);
			clawGrips.get(index)
				.setTransform(poseStack)
				.setChanged();
			poseStack.popPose();
		}

		poseStack.popPose();
	}

	@Override
	public void update(float pt) {
		super.update(pt);
		instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(blockEntity.goggles ? AllPartialModels.ARM_CLAW_BASE_GOGGLES : AllPartialModels.ARM_CLAW_BASE))
			.stealInstance(claw);
	}

	@Override
	public void updateLight(float partialTick) {
		super.updateLight(partialTick);

		relight(models.toArray(FlatLit[]::new));
	}

	@Override
	protected void _delete() {
		super._delete();
		models.forEach(AbstractInstance::delete);
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		super.collectCrumblingInstances(consumer);
		models.forEach(consumer);
	}
}
