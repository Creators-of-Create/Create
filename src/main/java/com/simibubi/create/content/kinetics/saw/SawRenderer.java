package com.simibubi.create.content.kinetics.saw;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.math.AngleHelper;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SawRenderer extends SafeBlockEntityRenderer<SawBlockEntity> {

	public SawRenderer(BlockEntityRendererProvider.Context context) {}

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

		SuperByteBuffer superBuffer = CachedBuffers.partialFacing(partial, blockState);
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

	protected void renderItems(SawBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light,
		int overlay) {
		if (be.getBlockState()
			.getValue(SawBlock.FACING) != Direction.UP)
			return;
		if (be.inventory.isEmpty())
			return;

		boolean alongZ = !be.getBlockState()
			.getValue(SawBlock.AXIS_ALONG_FIRST_COORDINATE);

		float duration = be.inventory.recipeDuration;
		boolean moving = duration != 0;
		float offset = moving ? (float) (be.inventory.remainingTime) / duration : 0;
		float processingSpeed = Mth.clamp(Math.abs(be.getSpeed()) / 32, 1, 128);
		if (moving) {
			offset = Mth.clamp(offset + ((-partialTicks + .5f) * processingSpeed) / duration, 0.125f, 1f);
			if (!be.inventory.appliedRecipe)
				offset += 1;
			offset /= 2;
		}

		if (be.getSpeed() == 0)
			offset = .5f;
		if (be.getSpeed() < 0 ^ alongZ)
			offset = 1 - offset;

		int outputs = 0;
		for (int i = 1; i < be.inventory.getSlots(); i++)
			if (!be.inventory.getStackInSlot(i)
				.isEmpty())
				outputs++;

		ms.pushPose();
		if (alongZ)
			ms.mulPose(Axis.YP.rotationDegrees(90));
		ms.translate(outputs <= 1 ? .5 : .25, 0, offset);
		ms.translate(alongZ ? -1 : 0, 0, 0);

		int renderedI = 0;
		for (int i = 0; i < be.inventory.getSlots(); i++) {
			ItemStack stack = be.inventory.getStackInSlot(i);
			if (stack.isEmpty())
				continue;

			ItemRenderer itemRenderer = Minecraft.getInstance()
				.getItemRenderer();
			BakedModel modelWithOverrides = itemRenderer.getModel(stack, be.getLevel(), null, 0);
			boolean blockItem = modelWithOverrides.isGui3d();

			ms.pushPose();
			ms.translate(0, blockItem ? .925f : 13f / 16f, 0);

			if (i > 0 && outputs > 1) {
				ms.translate((0.5 / (outputs - 1)) * renderedI, 0, 0);
				TransformStack.of(ms)
					.nudge(i * 133);
			}

			boolean box = PackageItem.isPackage(stack);
			if (box) {
				ms.translate(0, 4 / 16f, 0);
				ms.scale(1.5f, 1.5f, 1.5f);
			} else
				ms.scale(.5f, .5f, .5f);

			if (!box)
				ms.mulPose(Axis.XP.rotationDegrees(90));

			itemRenderer.render(stack, ItemDisplayContext.FIXED, false, ms, buffer, light, overlay, modelWithOverrides);
			renderedI++;

			ms.popPose();
		}

		ms.popPose();
	}

	protected SuperByteBuffer getRotatedModel(KineticBlockEntity be) {
		BlockState state = be.getBlockState();
		if (state.getValue(FACING)
			.getAxis()
			.isHorizontal())
			return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF,
				state.rotate(be.getLevel(), be.getBlockPos(), Rotation.CLOCKWISE_180));
		return CachedBuffers.block(KineticBlockEntityRenderer.KINETIC_BLOCK, getRenderedBlockState(be));
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
				superBuffer = CachedBuffers.partial(AllPartialModels.SAW_BLADE_HORIZONTAL_ACTIVE, state);
			else
				superBuffer = CachedBuffers.partial(AllPartialModels.SAW_BLADE_HORIZONTAL_INACTIVE, state);
		} else {
			if (shouldAnimate)
				superBuffer = CachedBuffers.partial(AllPartialModels.SAW_BLADE_VERTICAL_ACTIVE, state);
			else
				superBuffer = CachedBuffers.partial(AllPartialModels.SAW_BLADE_VERTICAL_INACTIVE, state);
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
