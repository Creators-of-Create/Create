package com.simibubi.create.content.contraptions.actors.contraptionControls;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.actors.contraptionControls.ContraptionControlsMovement.ElevatorFloorSelection;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.redstone.nixieTube.NixieTubeRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.simibubi.create.foundation.utility.DyeHelper;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ContraptionControlsRenderer extends SmartBlockEntityRenderer<ContraptionControlsBlockEntity> {
	public ContraptionControlsRenderer(Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(ContraptionControlsBlockEntity blockEntity, float pt, PoseStack ms,
							  MultiBufferSource buffer, int light, int overlay) {
		BlockState blockState = blockEntity.getBlockState();
		Direction facing = blockState.getValue(ContraptionControlsBlock.FACING)
			.getOpposite();
		Vec3 buttonMovementAxis = VecHelper.rotate(new Vec3(0, 1, -.325), AngleHelper.horizontalAngle(facing), Axis.Y);
		Vec3 buttonMovement = buttonMovementAxis.scale(-0.07f + -1 / 24f * blockEntity.button.getValue(pt));
		Vec3 buttonOffset = buttonMovementAxis.scale(0.07f);

		ms.pushPose();
		ms.translate(buttonMovement.x, buttonMovement.y, buttonMovement.z);
		super.renderSafe(blockEntity, pt, ms, buffer, light, overlay);
		ms.translate(buttonOffset.x, buttonOffset.y, buttonOffset.z);

		VertexConsumer vc = buffer.getBuffer(RenderType.solid());
		CachedBuffers.partialFacing(AllPartialModels.CONTRAPTION_CONTROLS_BUTTON, blockState, facing)
			.light(light)
			.renderInto(ms, vc);

		ms.popPose();

		int i = (((int) blockEntity.indicator.getValue(pt) / 45) % 8) + 8;
		CachedBuffers.partialFacing(AllPartialModels.CONTRAPTION_CONTROLS_INDICATOR.get(i % 8), blockState, facing)
			.light(light)
			.renderInto(ms, vc);
	}

	public static void renderInContraption(MovementContext ctx, VirtualRenderWorld renderWorld,
										   ContraptionMatrices matrices, MultiBufferSource buffer) {

		if (!(ctx.temporaryData instanceof ElevatorFloorSelection efs))
			return;
		if (!AllBlocks.CONTRAPTION_CONTROLS.has(ctx.state))
			return;

		Entity cameraEntity = Minecraft.getInstance()
			.getCameraEntity();
		float playerDistance = (float) (ctx.position == null || cameraEntity == null ? 0
			: ctx.position.distanceToSqr(cameraEntity.getEyePosition()));

		float flicker = renderWorld.random.nextFloat();
		Couple<Integer> couple = DyeHelper.getDyeColors(efs.targetYEqualsSelection ? DyeColor.WHITE : DyeColor.ORANGE);
		int brightColor = couple.getFirst();
		int darkColor = couple.getSecond();
		int flickeringBrightColor = Color.mixColors(brightColor, darkColor, flicker / 4);
		Font fontRenderer = Minecraft.getInstance().font;
		float shadowOffset = .5f;

		String text = efs.currentShortName;
		String description = efs.currentLongName;
		PoseStack ms = matrices.getViewProjection();
		var msr = TransformStack.of(ms);

		float buttondepth = 0;
		if (ctx.contraption.getBlockEntityClientSide(ctx.localPos) instanceof ContraptionControlsBlockEntity cbe)
			buttondepth = -1 / 24f * cbe.button.getValue(AnimationTickHolder.getPartialTicks(renderWorld));

		ms.pushPose();
		msr.translate(ctx.localPos);
		ms.translate(0, buttondepth, 0);
		VertexConsumer vc = buffer.getBuffer(RenderType.solid());
		CachedBuffers.partialFacing(AllPartialModels.CONTRAPTION_CONTROLS_BUTTON, ctx.state, ctx.state.getValue(ContraptionControlsBlock.FACING).getOpposite())
			.light(LevelRenderer.getLightColor(renderWorld, ctx.localPos))
			.useLevelLight(ctx.world, matrices.getWorld())
			.renderInto(ms, vc);
		ms.popPose();

		ms.pushPose();
		msr.translate(ctx.localPos);
		msr.rotateCentered(AngleHelper.rad(AngleHelper.horizontalAngle(ctx.state.getValue(ContraptionControlsBlock.FACING))),
			Direction.UP);
		ms.translate(0.275f + 0.125f, 1 + 2 / 16f, 0.5f);
		msr.rotate(AngleHelper.rad(67.5f), Direction.WEST);

		if (!text.isBlank() && playerDistance < 100) {
			int actualWidth = fontRenderer.width(text);
			int width = Math.max(actualWidth, 12);
			float scale = 1 / (5f * (width - .5f));
			float heightCentering = (width - 8f) / 2;

			ms.pushPose();
			ms.translate(0, .15f, buttondepth - .25f);
			ms.scale(scale, -scale, scale);
			ms.translate((float) Math.max(0, width - actualWidth) / 2, heightCentering, 0);
			NixieTubeRenderer.drawInWorldString(ms, buffer, text, flickeringBrightColor);
			ms.translate(shadowOffset, shadowOffset, -1 / 16f);
			NixieTubeRenderer.drawInWorldString(ms, buffer, text, Color.mixColors(darkColor, 0, .35f));
			ms.popPose();
		}

		if (!description.isBlank() && playerDistance < 20) {
			int actualWidth = fontRenderer.width(description);
			int width = Math.max(actualWidth, 55);
			float scale = 1 / (3f * (width - .5f));
			float heightCentering = (width - 8f) / 2;

			ms.pushPose();
			ms.translate(-.0635f, 0.06f, buttondepth - .25f);
			ms.scale(scale, -scale, scale);
			ms.translate((float) Math.max(0, width - actualWidth) / 2, heightCentering, 0);
			NixieTubeRenderer.drawInWorldString(ms, buffer, description, flickeringBrightColor);
			ms.popPose();
		}

		ms.popPose();

	}
}
