package com.simibubi.create.content.logistics.block.mechanicalArm;

import java.util.ArrayList;

import com.google.common.collect.Lists;
import com.jozufozu.flywheel.backend.instancing.IDynamicInstance;
import com.jozufozu.flywheel.backend.instancing.InstanceData;
import com.jozufozu.flywheel.backend.material.InstanceMaterial;
import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.jozufozu.flywheel.core.materials.ModelData;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class ArmInstance extends SingleRotatingInstance implements IDynamicInstance {

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

	public ArmInstance(MaterialManager<?> modelManager, ArmTileEntity tile) {
		super(modelManager, tile);

		InstanceMaterial<ModelData> mat = getTransformMaterial();

		base = mat.getModel(AllBlockPartials.ARM_BASE, blockState).createInstance();
		lowerBody = mat.getModel(AllBlockPartials.ARM_LOWER_BODY, blockState).createInstance();
		upperBody = mat.getModel(AllBlockPartials.ARM_UPPER_BODY, blockState).createInstance();
		head = mat.getModel(AllBlockPartials.ARM_HEAD, blockState).createInstance();
		claw = mat.getModel(AllBlockPartials.ARM_CLAW_BASE, blockState).createInstance();

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
		if (arm.phase == ArmTileEntity.Phase.DANCING && tile.getSpeed() != 0) {
			animateArm(true);
			firstRender = true;
			return;
		}

		float pt = AnimationTickHolder.getPartialTicks();

		float baseAngleNow = this.arm.baseAngle.get(pt);
		float lowerArmAngleNow = this.arm.lowerArmAngle.get(pt);
		float upperArmAngleNow = this.arm.upperArmAngle.get(pt);
		float headAngleNow = this.arm.headAngle.get(pt);

		boolean settled = MathHelper.equal(baseAngle, baseAngleNow)
				&& MathHelper.equal(lowerArmAngle, lowerArmAngleNow)
				&& MathHelper.equal(upperArmAngle, upperArmAngleNow)
				&& MathHelper.equal(headAngle, headAngleNow);

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
			float renderTick = AnimationTickHolder.getRenderTime(this.arm.getLevel()) + (tile.hashCode() % 64);
			baseAngle = (renderTick * 10) % 360;
			lowerArmAngle = MathHelper.lerp((MathHelper.sin(renderTick / 4) + 1) / 2, -45, 15);
			upperArmAngle = MathHelper.lerp((MathHelper.sin(renderTick / 8) + 1) / 4, -45, 95);
			headAngle = -lowerArmAngle;
			color = ColorHelper.rainbowColor(AnimationTickHolder.getTicks() * 100);
		} else {
			baseAngle = this.baseAngle;
			lowerArmAngle = this.lowerArmAngle - 135;
			upperArmAngle = this.upperArmAngle - 90;
			headAngle = this.headAngle;
			color = 0xFFFFFF;
		}

		MatrixStack msLocal = new MatrixStack();
		MatrixTransformStack msr = MatrixTransformStack.of(msLocal);
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
				&& itemRenderer.getModel(item, Minecraft.getInstance().level, null)
				.isGui3d();

		for (int index : Iterate.zeroAndOne) {
			msLocal.pushPose();
			int flip = index * 2 - 1;
			ArmRenderer.transformClawHalf(msr, hasItem, isBlockItem, flip);
			clawGrips.get(index).setTransform(msLocal);
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
		return getRotatingMaterial().getModel(AllBlockPartials.ARM_COG, tile.getBlockState());
	}

	@Override
	public void remove() {
		super.remove();
		models.forEach(InstanceData::delete);
	}

}
