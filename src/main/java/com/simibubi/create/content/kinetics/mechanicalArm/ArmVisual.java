package com.simibubi.create.content.kinetics.mechanicalArm;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.google.common.collect.Lists;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.AbstractInstance;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.model.Models;
import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.SingleRotatingVisual;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class ArmVisual extends SingleRotatingVisual<ArmBlockEntity> implements DynamicVisual {

	final TransformedInstance base;
	final TransformedInstance lowerBody;
	final TransformedInstance upperBody;
	TransformedInstance claw;

	private final ArrayList<TransformedInstance> clawGrips;
	private final ArrayList<TransformedInstance> models;
	private final Boolean ceiling;

	private boolean firstRender = true;

	private float baseAngle = Float.NaN;
	private float lowerArmAngle = Float.NaN;
	private float upperArmAngle = Float.NaN;
	private float headAngle = Float.NaN;

	public ArmVisual(VisualizationContext context, ArmBlockEntity blockEntity) {
		super(context, blockEntity);

		base = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ARM_BASE), RenderStage.AFTER_BLOCK_ENTITIES)
			.createInstance();
		lowerBody = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ARM_LOWER_BODY), RenderStage.AFTER_BLOCK_ENTITIES)
			.createInstance();
		upperBody = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ARM_UPPER_BODY), RenderStage.AFTER_BLOCK_ENTITIES)
			.createInstance();
		claw = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(blockEntity.goggles ? AllPartialModels.ARM_CLAW_BASE_GOGGLES : AllPartialModels.ARM_CLAW_BASE), RenderStage.AFTER_BLOCK_ENTITIES)
			.createInstance();

		TransformedInstance clawGrip1 = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ARM_CLAW_GRIP_UPPER), RenderStage.AFTER_BLOCK_ENTITIES)
			.createInstance();
		TransformedInstance clawGrip2 = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ARM_CLAW_GRIP_LOWER), RenderStage.AFTER_BLOCK_ENTITIES)
			.createInstance();

		clawGrips = Lists.newArrayList(clawGrip1, clawGrip2);
		models = Lists.newArrayList(base, lowerBody, upperBody, claw, clawGrip1, clawGrip2);
		ceiling = blockState.getValue(ArmBlock.CEILING);

		animateArm(false);
	}

	@Override
	public void beginFrame(VisualFrameContext ctx) {
		if (blockEntity.phase == ArmBlockEntity.Phase.DANCING && blockEntity.getSpeed() != 0) {
			animateArm(true);
			firstRender = true;
			return;
		}

		float pt = AnimationTickHolder.getPartialTicks();

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

		if (!settled || firstRender)
			animateArm(false);

		if (firstRender)
			firstRender = false;
	}

	private void animateArm(boolean rave) {
		float baseAngle;
		float lowerArmAngle;
		float upperArmAngle;
		float headAngle;
		int color;

		if (rave) {
			float renderTick =
				AnimationTickHolder.getRenderTime(blockEntity.getLevel()) + (blockEntity.hashCode() % 64);
			baseAngle = (renderTick * 10) % 360;
			lowerArmAngle = Mth.lerp((Mth.sin(renderTick / 4) + 1) / 2, -45, 15);
			upperArmAngle = Mth.lerp((Mth.sin(renderTick / 8) + 1) / 4, -45, 95);
			headAngle = -lowerArmAngle;
			color = Color.rainbowColor(AnimationTickHolder.getTicks() * 100)
				.getRGB();
		} else {
			baseAngle = this.baseAngle;
			lowerArmAngle = this.lowerArmAngle - 135;
			upperArmAngle = this.upperArmAngle - 90;
			headAngle = this.headAngle;
			color = 0xFFFFFF;
		}

		PoseStack msLocal = new PoseStack();
		var msr = TransformStack.of(msLocal);
		msr.translate(getVisualPosition());
		msr.center();

		if (ceiling)
			msr.rotateXDegrees(180);

		ArmRenderer.transformBase(msr, baseAngle);
		base.setTransform(msLocal)
			.setChanged();

		ArmRenderer.transformLowerArm(msr, lowerArmAngle);
		lowerBody.setTransform(msLocal)
			.setColor(color)
			.setChanged();

		ArmRenderer.transformUpperArm(msr, upperArmAngle);
		upperBody.setTransform(msLocal)
			.setColor(color)
			.setChanged();

		ArmRenderer.transformHead(msr, headAngle);

		if (ceiling && blockEntity.goggles)
			msr.rotateZDegrees(180);

		claw.setTransform(msLocal)
			.setChanged();

		if (ceiling && blockEntity.goggles)
			msr.rotateZDegrees(180);

		ItemStack item = blockEntity.heldItem;
		ItemRenderer itemRenderer = Minecraft.getInstance()
			.getItemRenderer();
		boolean hasItem = !item.isEmpty();
		boolean isBlockItem = hasItem && (item.getItem() instanceof BlockItem)
			&& itemRenderer.getModel(item, Minecraft.getInstance().level, null, 0)
				.isGui3d();

		for (int index : Iterate.zeroAndOne) {
			msLocal.pushPose();
			int flip = index * 2 - 1;
			ArmRenderer.transformClawHalf(msr, hasItem, isBlockItem, flip);
			clawGrips.get(index)
				.setTransform(msLocal)
				.setChanged();
			msLocal.popPose();
		}
	}

	@Override
	public void update(float pt) {
		super.update(pt);
		models.remove(claw);
		claw.delete();
		claw = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(blockEntity.goggles ? AllPartialModels.ARM_CLAW_BASE_GOGGLES : AllPartialModels.ARM_CLAW_BASE), RenderStage.AFTER_BLOCK_ENTITIES)
				.createInstance();
		models.add(claw);
		updateLight();
		animateArm(false);
	}

	@Override
	public void updateLight() {
		super.updateLight();

		relight(pos, models.stream());
	}

	@Override
	protected Model model() {
		return Models.partial(AllPartialModels.ARM_COG);
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
