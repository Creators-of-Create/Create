package com.simibubi.create.content.kinetics.saw;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SawRenderer extends SafeBlockEntityRenderer<SawBlockEntity> {

	public SawRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	protected void renderSafe(SawBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light,
		int overlay) {
		renderBlade(be, ms, buffer, light);
		renderItems(be, partialTicks, ms, buffer, light, overlay);
		FilteringRenderer.renderOnBlockEntity(be, partialTicks, ms, buffer, light, overlay);

		if (VisualizationManager.supportsVisualization(be.getLevel()))
			return;

		renderShaft(be, ms, buffer, light, overlay);
	}

	protected void renderBlade(SawBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light) {
		BlockState blockState = be.getBlockState();
		PartialModel partial;
		float speed = be.getSpeed();
		boolean rotate = false;

		if (SawBlock.isHorizontal(blockState)) {
			if (speed > 0) {
				partial = AllPartialModels.SAW_BLADE_HORIZONTAL_ACTIVE;
			} else if (speed < 0) {
				partial = AllPartialModels.SAW_BLADE_HORIZONTAL_REVERSED;
			} else {
				partial = AllPartialModels.SAW_BLADE_HORIZONTAL_INACTIVE;
			}
		} else {
			if (be.getSpeed() > 0) {
				partial = AllPartialModels.SAW_BLADE_VERTICAL_ACTIVE;
			} else if (speed < 0) {
				partial = AllPartialModels.SAW_BLADE_VERTICAL_REVERSED;
			} else {
				partial = AllPartialModels.SAW_BLADE_VERTICAL_INACTIVE;
			}

			if (blockState.getValue(SawBlock.AXIS_ALONG_FIRST_COORDINATE))
				rotate = true;
		}

		SuperByteBuffer superBuffer = CachedBufferer.partialFacing(partial, blockState);
		if (rotate) {
			superBuffer.rotateCentered(AngleHelper.rad(90), Direction.UP);
		}
		superBuffer.color(0xFFFFFF)
			.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
	}

	protected void renderShaft(SawBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		KineticBlockEntityRenderer.renderRotatingBuffer(be, getRotatedModel(be), ms,
			buffer.getBuffer(RenderType.solid()), light);
	}

	protected void renderItems(SawBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		boolean processingMode = be.getBlockState()
			.getValue(SawBlock.FACING) == Direction.UP;
		if (processingMode && !be.inventory.isEmpty()) {
			boolean alongZ = !be.getBlockState()
				.getValue(SawBlock.AXIS_ALONG_FIRST_COORDINATE);
			ms.pushPose();

			boolean moving = be.inventory.recipeDuration != 0;
			float offset = moving ? (float) (be.inventory.remainingTime) / be.inventory.recipeDuration : 0;
			float processingSpeed = Mth.clamp(Math.abs(be.getSpeed()) / 32, 1, 128);
			if (moving) {
				offset = Mth
					.clamp(offset + ((-partialTicks + .5f) * processingSpeed) / be.inventory.recipeDuration, 0.125f, 1f);
				if (!be.inventory.appliedRecipe)
					offset += 1;
				offset /= 2;
			}

			if (be.getSpeed() == 0)
				offset = .5f;
			if (be.getSpeed() < 0 ^ alongZ)
				offset = 1 - offset;

			for (int i = 0; i < be.inventory.getSlots(); i++) {
				ItemStack stack = be.inventory.getStackInSlot(i);
				if (stack.isEmpty())
					continue;

				ItemRenderer itemRenderer = Minecraft.getInstance()
					.getItemRenderer();
				BakedModel modelWithOverrides = itemRenderer.getModel(stack, be.getLevel(), null, 0);
				boolean blockItem = modelWithOverrides.isGui3d();

				ms.translate(alongZ ? offset : .5, blockItem ? .925f : 13f / 16f, alongZ ? .5 : offset);

				ms.scale(.5f, .5f, .5f);
				if (alongZ)
					ms.mulPose(Axis.YP.rotationDegrees(90));
				ms.mulPose(Axis.XP.rotationDegrees(90));
				itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, ms, buffer, be.getLevel(), 0);
				break;
			}

			ms.popPose();
		}
	}

	protected SuperByteBuffer getRotatedModel(KineticBlockEntity be) {
		BlockState state = be.getBlockState();
		if (state.getValue(FACING)
			.getAxis()
			.isHorizontal())
			return CachedBufferer.partialFacing(AllPartialModels.SHAFT_HALF,
				state.rotate(be.getLevel(), be.getBlockPos(), Rotation.CLOCKWISE_180));
		return CachedBufferer.block(KineticBlockEntityRenderer.KINETIC_BLOCK,
			getRenderedBlockState(be));
	}

	protected BlockState getRenderedBlockState(KineticBlockEntity be) {
		return KineticBlockEntityRenderer.shaft(KineticBlockEntityRenderer.getRotationAxisOf(be));
	}

	public static void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
		ContraptionMatrices matrices, MultiBufferSource buffer) {
		BlockState state = context.state;
		Direction facing = state.getValue(SawBlock.FACING);

		Vec3 facingVec = Vec3.atLowerCornerOf(context.state.getValue(SawBlock.FACING)
			.getNormal());
		facingVec = context.rotation.apply(facingVec);

		Direction closestToFacing = Direction.getNearest(facingVec.x, facingVec.y, facingVec.z);

		boolean horizontal = closestToFacing.getAxis()
			.isHorizontal();
		boolean backwards = VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite());
		boolean moving = context.getAnimationSpeed() != 0;
		boolean shouldAnimate =
			(context.contraption.stalled && horizontal) || (!context.contraption.stalled && !backwards && moving);

		SuperByteBuffer superBuffer;
		if (SawBlock.isHorizontal(state)) {
			if (shouldAnimate)
				superBuffer = CachedBufferer.partial(AllPartialModels.SAW_BLADE_HORIZONTAL_ACTIVE, state);
			else
				superBuffer = CachedBufferer.partial(AllPartialModels.SAW_BLADE_HORIZONTAL_INACTIVE, state);
		} else {
			if (shouldAnimate)
				superBuffer = CachedBufferer.partial(AllPartialModels.SAW_BLADE_VERTICAL_ACTIVE, state);
			else
				superBuffer = CachedBufferer.partial(AllPartialModels.SAW_BLADE_VERTICAL_INACTIVE, state);
		}

		superBuffer.transform(matrices.getModel())
			.center()
			.rotateYDegrees(AngleHelper.horizontalAngle(facing))
			.rotateXDegrees(AngleHelper.verticalAngle(facing));

		if (!SawBlock.isHorizontal(state)) {
			superBuffer.rotateZDegrees(state.getValue(SawBlock.AXIS_ALONG_FIRST_COORDINATE) ? 90 : 0);
		}

		superBuffer.uncenter()
			.light(LevelRenderer.getLightColor(renderWorld, context.localPos))
			.useLevelLight(context.world, matrices.getWorld())
			.renderInto(matrices.getViewProjection(), buffer.getBuffer(RenderType.cutoutMipped()));
	}

}
