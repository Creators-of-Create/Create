package com.simibubi.create.content.logistics.block.mechanicalArm;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmTileEntity.Phase;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class ArmRenderer extends KineticTileEntityRenderer {

	public ArmRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public boolean shouldRenderOffScreen(KineticTileEntity te) {
		return true;
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float pt, PoseStack ms, MultiBufferSource buffer, int light,
		int overlay) {
		super.renderSafe(te, pt, ms, buffer, light, overlay);

		ArmTileEntity arm = (ArmTileEntity) te;
		ItemStack item = arm.heldItem;
		boolean hasItem = !item.isEmpty();
		boolean usingFlywheel = Backend.getInstance().canUseInstancing(te.getLevel());

		if (usingFlywheel && !hasItem) return;

		ItemRenderer itemRenderer = Minecraft.getInstance()
											 .getItemRenderer();

		boolean isBlockItem = hasItem && (item.getItem() instanceof BlockItem)
				&& itemRenderer.getModel(item, te.getLevel(), null, 0)
							   .isGui3d();

		VertexConsumer builder = buffer.getBuffer(RenderType.solid());
		BlockState blockState = te.getBlockState();

		PoseStack msLocal = new PoseStack();
		MatrixTransformStack msr = MatrixTransformStack.of(msLocal);

		float baseAngle;
		float lowerArmAngle;
		float upperArmAngle;
		float headAngle;
		int color;

		boolean rave = arm.phase == Phase.DANCING && te.getSpeed() != 0;
		if (rave) {
			float renderTick = AnimationTickHolder.getRenderTime(te.getLevel()) + (te.hashCode() % 64);
			baseAngle = (renderTick * 10) % 360;
			lowerArmAngle = Mth.lerp((Mth.sin(renderTick / 4) + 1) / 2, -45, 15);
			upperArmAngle = Mth.lerp((Mth.sin(renderTick / 8) + 1) / 4, -45, 95);
			headAngle = -lowerArmAngle;
			color = Color.rainbowColor(AnimationTickHolder.getTicks() * 100).getRGB();
		} else {
			baseAngle = arm.baseAngle.get(pt);
			lowerArmAngle = arm.lowerArmAngle.get(pt) - 135;
			upperArmAngle = arm.upperArmAngle.get(pt) - 90;
			headAngle = arm.headAngle.get(pt);
			color = 0xFFFFFF;
		}

		msr.centre();

		if (blockState.getValue(ArmBlock.CEILING))
			msr.rotateX(180);

		if (usingFlywheel)
			doItemTransforms(msr, baseAngle, lowerArmAngle, upperArmAngle, headAngle);
		else
			renderArm(builder, ms, msLocal, msr, blockState, color, baseAngle, lowerArmAngle, upperArmAngle, headAngle, hasItem, isBlockItem, light);

		if (hasItem) {
			ms.pushPose();
			float itemScale = isBlockItem ? .5f : .625f;
			msr.rotateX(90);
			msLocal.translate(0, -4 / 16f, 0);
			msLocal.scale(itemScale, itemScale, itemScale);

			ms.last().pose().multiply(msLocal.last().pose());

			itemRenderer
				.renderStatic(item, TransformType.FIXED, light, overlay, ms, buffer, 0);
			ms.popPose();
		}

	}

	private void renderArm(VertexConsumer builder, PoseStack ms, PoseStack msLocal, MatrixTransformStack msr, BlockState blockState, int color, float baseAngle, float lowerArmAngle, float upperArmAngle, float headAngle, boolean hasItem, boolean isBlockItem, int light) {
		SuperByteBuffer base = PartialBufferer.get(AllBlockPartials.ARM_BASE, blockState).light(light);
		SuperByteBuffer lowerBody = PartialBufferer.get(AllBlockPartials.ARM_LOWER_BODY, blockState).light(light);
		SuperByteBuffer upperBody = PartialBufferer.get(AllBlockPartials.ARM_UPPER_BODY, blockState).light(light);
		SuperByteBuffer head = PartialBufferer.get(AllBlockPartials.ARM_HEAD, blockState).light(light);
		SuperByteBuffer claw = PartialBufferer.get(AllBlockPartials.ARM_CLAW_BASE, blockState).light(light);
		SuperByteBuffer clawGrip = PartialBufferer.get(AllBlockPartials.ARM_CLAW_GRIP, blockState);

		transformBase(msr, baseAngle);
		base.transform(msLocal)
				.renderInto(ms, builder);

		transformLowerArm(msr, lowerArmAngle);
		lowerBody.color(color)
				.transform(msLocal)
				.renderInto(ms, builder);

		transformUpperArm(msr, upperArmAngle);
		upperBody.color(color)
				 .transform(msLocal)
				 .renderInto(ms, builder);

		transformHead(msr, headAngle);
		head.transform(msLocal)
			.renderInto(ms, builder);

		transformClaw(msr);
		claw.transform(msLocal)
			.renderInto(ms, builder);

		for (int flip : Iterate.positiveAndNegative) {
			msLocal.pushPose();
			transformClawHalf(msr, hasItem, isBlockItem, flip);
			clawGrip.light(light).transform(msLocal).renderInto(ms, builder);
			msLocal.popPose();
		}
	}

	private void doItemTransforms(MatrixTransformStack msr, float baseAngle, float lowerArmAngle, float upperArmAngle, float headAngle) {

		transformBase(msr, baseAngle);
		transformLowerArm(msr, lowerArmAngle);
		transformUpperArm(msr, upperArmAngle);
		transformHead(msr, headAngle);
		transformClaw(msr);
	}

	public static void transformClawHalf(MatrixTransformStack msr, boolean hasItem, boolean isBlockItem, int flip) {
		msr.translate(0, flip * 3 / 16d, -1 / 16d);
		msr.rotateX(flip * (hasItem ? isBlockItem ? 0 : -35 : 0));
	}

	public static void transformClaw(MatrixTransformStack msr) {
		msr.translate(0, 0, -4 / 16d);
	}

	public static void transformHead(MatrixTransformStack msr, float headAngle) {
		msr.translate(0, 11 / 16d, -11 / 16d);
		msr.rotateX(headAngle);
	}

	public static void transformUpperArm(MatrixTransformStack msr, float upperArmAngle) {
		msr.translate(0, 12 / 16d, 12 / 16d);
		msr.rotateX(upperArmAngle);
	}

	public static void transformLowerArm(MatrixTransformStack msr, float lowerArmAngle) {
		msr.translate(0, 1 / 16d, -2 / 16d);
		msr.rotateX(lowerArmAngle);
		msr.translate(0, -1 / 16d, 0);
	}

	public static void transformBase(MatrixTransformStack msr, float baseAngle) {
		msr.translate(0, 4 / 16d, 0);
		msr.rotateY(baseAngle);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return PartialBufferer.get(AllBlockPartials.ARM_COG, te.getBlockState());
	}

}
