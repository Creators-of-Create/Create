package com.simibubi.create.content.logistics.block.mechanicalArm;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmBlockEntity.Phase;
import com.simibubi.create.foundation.render.CachedBufferer;
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

public class ArmRenderer extends KineticBlockEntityRenderer<ArmBlockEntity> {

	public ArmRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(ArmBlockEntity be, float pt, PoseStack ms, MultiBufferSource buffer, int light,
		int overlay) {
		super.renderSafe(be, pt, ms, buffer, light, overlay);

		ItemStack item = be.heldItem;
		boolean hasItem = !item.isEmpty();
		boolean usingFlywheel = Backend.canUseInstancing(be.getLevel());

		if (usingFlywheel && !hasItem)
			return;

		ItemRenderer itemRenderer = Minecraft.getInstance()
			.getItemRenderer();

		boolean isBlockItem =
			hasItem && (item.getItem() instanceof BlockItem) && itemRenderer.getModel(item, be.getLevel(), null, 0)
				.isGui3d();

		VertexConsumer builder = buffer.getBuffer(RenderType.solid());
		BlockState blockState = be.getBlockState();

		PoseStack msLocal = new PoseStack();
		TransformStack msr = TransformStack.cast(msLocal);

		float baseAngle;
		float lowerArmAngle;
		float upperArmAngle;
		float headAngle;
		int color;

		boolean rave = be.phase == Phase.DANCING && be.getSpeed() != 0;
		if (rave) {
			float renderTick = AnimationTickHolder.getRenderTime(be.getLevel()) + (be.hashCode() % 64);
			baseAngle = (renderTick * 10) % 360;
			lowerArmAngle = Mth.lerp((Mth.sin(renderTick / 4) + 1) / 2, -45, 15);
			upperArmAngle = Mth.lerp((Mth.sin(renderTick / 8) + 1) / 4, -45, 95);
			headAngle = -lowerArmAngle;
			color = Color.rainbowColor(AnimationTickHolder.getTicks() * 100)
				.getRGB();
		} else {
			baseAngle = be.baseAngle.getValue(pt);
			lowerArmAngle = be.lowerArmAngle.getValue(pt) - 135;
			upperArmAngle = be.upperArmAngle.getValue(pt) - 90;
			headAngle = be.headAngle.getValue(pt);
			color = 0xFFFFFF;
		}

		msr.centre();

		if (blockState.getValue(ArmBlock.CEILING))
			msr.rotateX(180);

		if (usingFlywheel)
			doItemTransforms(msr, baseAngle, lowerArmAngle, upperArmAngle, headAngle);
		else
			renderArm(builder, ms, msLocal, msr, blockState, color, baseAngle, lowerArmAngle, upperArmAngle, headAngle,
				hasItem, isBlockItem, light);

		if (hasItem) {
			ms.pushPose();
			float itemScale = isBlockItem ? .5f : .625f;
			msr.rotateX(90);
			msLocal.translate(0, -4 / 16f, 0);
			msLocal.scale(itemScale, itemScale, itemScale);

			ms.last()
				.pose()
				.multiply(msLocal.last()
					.pose());

			itemRenderer.renderStatic(item, TransformType.FIXED, light, overlay, ms, buffer, 0);
			ms.popPose();
		}

	}

	private void renderArm(VertexConsumer builder, PoseStack ms, PoseStack msLocal, TransformStack msr,
		BlockState blockState, int color, float baseAngle, float lowerArmAngle, float upperArmAngle, float headAngle,
		boolean hasItem, boolean isBlockItem, int light) {
		SuperByteBuffer base = CachedBufferer.partial(AllPartialModels.ARM_BASE, blockState)
			.light(light);
		SuperByteBuffer lowerBody = CachedBufferer.partial(AllPartialModels.ARM_LOWER_BODY, blockState)
			.light(light);
		SuperByteBuffer upperBody = CachedBufferer.partial(AllPartialModels.ARM_UPPER_BODY, blockState)
			.light(light);
		SuperByteBuffer head = CachedBufferer.partial(AllPartialModels.ARM_HEAD, blockState)
			.light(light);
		SuperByteBuffer claw = CachedBufferer.partial(AllPartialModels.ARM_CLAW_BASE, blockState)
			.light(light);
		SuperByteBuffer clawGrip = CachedBufferer.partial(AllPartialModels.ARM_CLAW_GRIP, blockState);

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
			clawGrip.light(light)
				.transform(msLocal)
				.renderInto(ms, builder);
			msLocal.popPose();
		}
	}

	private void doItemTransforms(TransformStack msr, float baseAngle, float lowerArmAngle, float upperArmAngle,
		float headAngle) {

		transformBase(msr, baseAngle);
		transformLowerArm(msr, lowerArmAngle);
		transformUpperArm(msr, upperArmAngle);
		transformHead(msr, headAngle);
		transformClaw(msr);
	}

	public static void transformClawHalf(TransformStack msr, boolean hasItem, boolean isBlockItem, int flip) {
		msr.translate(0, flip * 3 / 16d, -1 / 16d);
		msr.rotateX(flip * (hasItem ? isBlockItem ? 0 : -35 : 0));
	}

	public static void transformClaw(TransformStack msr) {
		msr.translate(0, 0, -4 / 16d);
	}

	public static void transformHead(TransformStack msr, float headAngle) {
		msr.translate(0, 11 / 16d, -11 / 16d);
		msr.rotateX(headAngle);
	}

	public static void transformUpperArm(TransformStack msr, float upperArmAngle) {
		msr.translate(0, 12 / 16d, 12 / 16d);
		msr.rotateX(upperArmAngle);
	}

	public static void transformLowerArm(TransformStack msr, float lowerArmAngle) {
		msr.translate(0, 1 / 16d, -2 / 16d);
		msr.rotateX(lowerArmAngle);
		msr.translate(0, -1 / 16d, 0);
	}

	public static void transformBase(TransformStack msr, float baseAngle) {
		msr.translate(0, 4 / 16d, 0);
		msr.rotateY(baseAngle);
	}

	@Override
	public boolean shouldRenderOffScreen(ArmBlockEntity be) {
		return true;
	}

	@Override
	protected SuperByteBuffer getRotatedModel(ArmBlockEntity be, BlockState state) {
		return CachedBufferer.partial(AllPartialModels.ARM_COG, state);
	}

}
