package com.simibubi.create.content.logistics.block.mechanicalArm;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.foundation.render.backend.RenderMaterials;
import com.simibubi.create.foundation.render.backend.instancing.*;
import com.simibubi.create.foundation.render.backend.instancing.impl.ModelData;
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

	final InstanceKey<ModelData> base;
	final InstanceKey<ModelData> lowerBody;
	final InstanceKey<ModelData> upperBody;
	final InstanceKey<ModelData> head;
	final InstanceKey<ModelData> claw;
	private final ArrayList<InstanceKey<ModelData>> clawGrips;

	private final ArrayList<InstanceKey<ModelData>> models;

	private boolean firstTick = true;

	public ArmInstance(InstancedTileRenderer<?> modelManager, KineticTileEntity tile) {
		super(modelManager, tile);

		RenderMaterial<?, InstancedModel<ModelData>> mat = modelManager.getMaterial(RenderMaterials.TRANSFORMED);

		base = mat.getModel(AllBlockPartials.ARM_BASE, blockState).createInstance();
		lowerBody = mat.getModel(AllBlockPartials.ARM_LOWER_BODY, blockState).createInstance();
		upperBody = mat.getModel(AllBlockPartials.ARM_UPPER_BODY, blockState).createInstance();
		head = mat.getModel(AllBlockPartials.ARM_HEAD, blockState).createInstance();
		claw = mat.getModel(AllBlockPartials.ARM_CLAW_BASE, blockState).createInstance();

		InstancedModel<ModelData> clawHalfModel = mat.getModel(AllBlockPartials.ARM_CLAW_GRIP, blockState);
		InstanceKey<ModelData> clawGrip1 = clawHalfModel.createInstance();
		InstanceKey<ModelData> clawGrip2 = clawHalfModel.createInstance();

		clawGrips = Lists.newArrayList(clawGrip1, clawGrip2);
		models = Lists.newArrayList(base, lowerBody, upperBody, head, claw, clawGrip1, clawGrip2);

		updateLight();
	}

	@Override
	public void beginFrame() {
		ArmTileEntity arm = (ArmTileEntity) tile;

		boolean settled = arm.baseAngle.settled() && arm.lowerArmAngle.settled() && arm.upperArmAngle.settled() && arm.headAngle.settled();
		boolean rave = arm.phase == ArmTileEntity.Phase.DANCING;

		if (!settled || rave || firstTick)
			transformModels(arm, rave);

		if (settled)
			firstTick = false;
	}

	private void transformModels(ArmTileEntity arm, boolean rave) {
		float pt = AnimationTickHolder.getPartialTicks();
		int color = 0xFFFFFF;

		float baseAngle = arm.baseAngle.get(pt);
		float lowerArmAngle = arm.lowerArmAngle.get(pt) - 135;
		float upperArmAngle = arm.upperArmAngle.get(pt) - 90;
		float headAngle = arm.headAngle.get(pt);

		if (rave) {
			float renderTick = AnimationTickHolder.getRenderTime(arm.getWorld()) + (tile.hashCode() % 64);
			baseAngle = (renderTick * 10) % 360;
			lowerArmAngle = MathHelper.lerp((MathHelper.sin(renderTick / 4) + 1) / 2, -45, 15);
			upperArmAngle = MathHelper.lerp((MathHelper.sin(renderTick / 8) + 1) / 4, -45, 95);
			headAngle = -lowerArmAngle;
			color = ColorHelper.rainbowColor(AnimationTickHolder.getTicks() * 100);
		}


		MatrixStack msLocal = new MatrixStack();
		MatrixStacker msr = MatrixStacker.of(msLocal);
		msr.translate(getFloatingPos());
		msr.centre();

		if (blockState.get(ArmBlock.CEILING))
			msr.rotateX(180);

		ArmRenderer.transformBase(msr, baseAngle);
		base.getInstance()
				.setTransform(msLocal);

		ArmRenderer.transformLowerArm(msr, lowerArmAngle);
		lowerBody.getInstance()
				.setTransform(msLocal)
				.setColor(color);

		ArmRenderer.transformUpperArm(msr, upperArmAngle);
		upperBody.getInstance()
				.setTransform(msLocal)
				.setColor(color);

		ArmRenderer.transformHead(msr, headAngle);
		head.getInstance()
				.setTransform(msLocal);

		ArmRenderer.transformClaw(msr);
		claw.getInstance()
				.setTransform(msLocal);

		ItemStack item = arm.heldItem;
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
			clawGrips.get(index)
					.getInstance()
					.setTransform(msLocal);
			msLocal.pop();
		}
	}

	@Override
	public void updateLight() {
		super.updateLight();

		relight(pos, models.stream().map(InstanceKey::getInstance));
	}

	@Override
	protected InstancedModel<RotatingData> getModel() {
		return AllBlockPartials.ARM_COG.renderOnRotating(modelManager, tile.getBlockState());
	}

	@Override
	public void remove() {
		super.remove();
		models.forEach(InstanceKey::delete);
	}
}
