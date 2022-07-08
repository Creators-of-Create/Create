package com.simibubi.create.content.logistics.block.mechanicalArm;

import java.util.ArrayList;

import com.google.common.collect.Lists;
import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.Material;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.content.contraptions.base.flwdata.RotatingData;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class ArmInstance extends SingleRotatingInstance implements DynamicInstance {

	final ModelData base;
	final ModelData lowerBody;
	final ModelData upperBody;
	final ModelData head;
	final ModelData claw;
	private final ArrayList<ModelData> clawGrips;

	private final ArrayList<ModelData> models;
	private final ArmTileEntity arm;
	private final Boolean ceiling;

	private boolean firstRender = true;

	private float baseAngle = Float.NaN;
	private float lowerArmAngle = Float.NaN;
	private float upperArmAngle = Float.NaN;
	private float headAngle = Float.NaN;

	public ArmInstance(MaterialManager modelManager, ArmTileEntity tile) {
		super(modelManager, tile);

		Material<ModelData> mat = getTransformMaterial();

		base = mat.getModel(AllBlockPartials.ARM_BASE, blockState)
			.createInstance();
		lowerBody = mat.getModel(AllBlockPartials.ARM_LOWER_BODY, blockState)
			.createInstance();
		upperBody = mat.getModel(AllBlockPartials.ARM_UPPER_BODY, blockState)
			.createInstance();
		head = mat.getModel(AllBlockPartials.ARM_HEAD, blockState)
			.createInstance();
		claw = mat.getModel(AllBlockPartials.ARM_CLAW_BASE, blockState)
			.createInstance();

		Instancer<ModelData> clawHalfModel = mat.getModel(AllBlockPartials.ARM_CLAW_GRIP, blockState);
		ModelData clawGrip1 = clawHalfModel.createInstance();
		ModelData clawGrip2 = clawHalfModel.createInstance();

		clawGrips = Lists.newArrayList(clawGrip1, clawGrip2);
		models = Lists.newArrayList(base, lowerBody, upperBody, head, claw, clawGrip1, clawGrip2);
		arm = tile;
		ceiling = blockState.getValue(ArmBlock.CEILING);

		animateArm(false);
	}

	@Override
	public void beginFrame() {
		if (arm.phase == ArmTileEntity.Phase.DANCING && blockEntity.getSpeed() != 0) {
			animateArm(true);
			firstRender = true;
			return;
		}

		float pt = AnimationTickHolder.getPartialTicks();

		float baseAngleNow = arm.baseAngle.getValue(pt);
		float lowerArmAngleNow = arm.lowerArmAngle.getValue(pt);
		float upperArmAngleNow = arm.upperArmAngle.getValue(pt);
		float headAngleNow = arm.headAngle.getValue(pt);

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
			float renderTick = AnimationTickHolder.getRenderTime(this.arm.getLevel()) + (blockEntity.hashCode() % 64);
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
		TransformStack msr = TransformStack.cast(msLocal);
		msr.translate(getInstancePosition());
		msr.centre();

		if (ceiling)
			msr.rotateX(180);

		ArmRenderer.transformBase(msr, baseAngle);
		base.setTransform(msLocal);

		ArmRenderer.transformLowerArm(msr, lowerArmAngle);
		lowerBody.setTransform(msLocal)
			.setColor(color);

		ArmRenderer.transformUpperArm(msr, upperArmAngle);
		upperBody.setTransform(msLocal)
			.setColor(color);

		ArmRenderer.transformHead(msr, headAngle);
		head.setTransform(msLocal);

		ArmRenderer.transformClaw(msr);
		claw.setTransform(msLocal);

		ItemStack item = this.arm.heldItem;
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
				.setTransform(msLocal);
			msLocal.popPose();
		}
	}

	@Override
	public void updateLight() {
		super.updateLight();

		relight(pos, models.stream());
	}

	@Override
	protected Instancer<RotatingData> getModel() {
		return getRotatingMaterial().getModel(AllBlockPartials.ARM_COG, blockEntity.getBlockState());
	}

	@Override
	public void remove() {
		super.remove();
		models.forEach(InstanceData::delete);
	}

}
