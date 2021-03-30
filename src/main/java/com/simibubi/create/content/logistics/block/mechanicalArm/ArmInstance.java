package com.simibubi.create.content.logistics.block.mechanicalArm;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.foundation.render.backend.instancing.*;
import com.simibubi.create.foundation.render.backend.core.ModelData;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.MatrixStacker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;

public class ArmInstance extends SingleRotatingInstance implements IDynamicInstance {

	final ModelData base;
	final ModelData lowerBody;
	final ModelData upperBody;
	final ModelData head;
	final ModelData claw;
	private final ArrayList<ModelData> clawGrips;

	private final ArrayList<ModelData> models;
	private final ArmTileEntity arm;

	private boolean firstTick = true;

	public ArmInstance(InstancedTileRenderer<?> modelManager, ArmTileEntity tile) {
		super(modelManager, tile);

		RenderMaterial<?, InstancedModel<ModelData>> mat = getTransformMaterial();

		base = mat.getModel(AllBlockPartials.ARM_BASE, blockState).createInstance();
		lowerBody = mat.getModel(AllBlockPartials.ARM_LOWER_BODY, blockState).createInstance();
		upperBody = mat.getModel(AllBlockPartials.ARM_UPPER_BODY, blockState).createInstance();
		head = mat.getModel(AllBlockPartials.ARM_HEAD, blockState).createInstance();
		claw = mat.getModel(AllBlockPartials.ARM_CLAW_BASE, blockState).createInstance();

		InstancedModel<ModelData> clawHalfModel = mat.getModel(AllBlockPartials.ARM_CLAW_GRIP, blockState);
		ModelData clawGrip1 = clawHalfModel.createInstance();
		ModelData clawGrip2 = clawHalfModel.createInstance();

		clawGrips = Lists.newArrayList(clawGrip1, clawGrip2);
		models = Lists.newArrayList(base, lowerBody, upperBody, head, claw, clawGrip1, clawGrip2);
		arm = tile;

		animateArm(false);
	}

	@Override
	public void beginFrame() {

		boolean settled = arm.baseAngle.settled() && arm.lowerArmAngle.settled() && arm.upperArmAngle.settled() && arm.headAngle.settled();
		boolean rave = arm.phase == ArmTileEntity.Phase.DANCING;

		if (!settled || rave || firstTick)
			animateArm(rave);

		if (settled)
			firstTick = false;
	}

	private void animateArm(boolean rave) {
		float pt = AnimationTickHolder.getPartialTicks();
		int color = 0xFFFFFF;

		float baseAngle = this.arm.baseAngle.get(pt);
		float lowerArmAngle = this.arm.lowerArmAngle.get(pt) - 135;
		float upperArmAngle = this.arm.upperArmAngle.get(pt) - 90;
		float headAngle = this.arm.headAngle.get(pt);

		if (rave) {
			float renderTick = AnimationTickHolder.getRenderTime(this.arm.getWorld()) + (tile.hashCode() % 64);
			baseAngle = (renderTick * 10) % 360;
			lowerArmAngle = MathHelper.lerp((MathHelper.sin(renderTick / 4) + 1) / 2, -45, 15);
			upperArmAngle = MathHelper.lerp((MathHelper.sin(renderTick / 8) + 1) / 4, -45, 95);
			headAngle = -lowerArmAngle;
			color = ColorHelper.rainbowColor(AnimationTickHolder.getTicks() * 100);
		}

		MatrixStack msLocal = new MatrixStack();
		MatrixStacker msr = MatrixStacker.of(msLocal);
		msr.translate(getInstancePosition());
		msr.centre();

		if (blockState.get(ArmBlock.CEILING))
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
				&& itemRenderer.getItemModelWithOverrides(item, Minecraft.getInstance().world, null)
				.isGui3d();

		for (int index : Iterate.zeroAndOne) {
			msLocal.push();
			int flip = index * 2 - 1;
			ArmRenderer.transformClawHalf(msr, hasItem, isBlockItem, flip);
			clawGrips.get(index).setTransform(msLocal);
			msLocal.pop();
		}
	}

	@Override
	public void updateLight() {
		super.updateLight();

		relight(pos, models.stream());
	}

	@Override
	protected InstancedModel<RotatingData> getModel() {
		return AllBlockPartials.ARM_COG.renderOnRotating(renderer, tile.getBlockState());
	}

	@Override
	public void remove() {
		super.remove();
		models.forEach(InstanceData::delete);
	}
}
