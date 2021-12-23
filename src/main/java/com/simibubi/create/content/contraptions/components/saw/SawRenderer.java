package com.simibubi.create.content.contraptions.components.saw;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SawRenderer extends SafeTileEntityRenderer<SawTileEntity> {

	public SawRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	protected void renderSafe(SawTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light,
		int overlay) {
		renderBlade(te, ms, buffer, light);
		renderItems(te, partialTicks, ms, buffer, light, overlay);
		FilteringRenderer.renderOnTileEntity(te, partialTicks, ms, buffer, light, overlay);

		if (Backend.canUseInstancing(te.getLevel()))
			return;

		renderShaft(te, ms, buffer, light, overlay);
	}

	protected void renderBlade(SawTileEntity te, PoseStack ms, MultiBufferSource buffer, int light) {
		BlockState blockState = te.getBlockState();
		PartialModel partial;
		float speed = te.getSpeed();
		boolean rotate = false;

		if (SawBlock.isHorizontal(blockState)) {
			if (speed > 0) {
				partial = AllBlockPartials.SAW_BLADE_HORIZONTAL_ACTIVE;
			} else if (speed < 0) {
				partial = AllBlockPartials.SAW_BLADE_HORIZONTAL_REVERSED;
			} else {
				partial = AllBlockPartials.SAW_BLADE_HORIZONTAL_INACTIVE;
			}
		} else {
			if (te.getSpeed() > 0) {
				partial = AllBlockPartials.SAW_BLADE_VERTICAL_ACTIVE;
			} else if (speed < 0) {
				partial = AllBlockPartials.SAW_BLADE_VERTICAL_REVERSED;
			} else {
				partial = AllBlockPartials.SAW_BLADE_VERTICAL_INACTIVE;
			}

			if (blockState.getValue(SawBlock.AXIS_ALONG_FIRST_COORDINATE))
				rotate = true;
		}

		SuperByteBuffer superBuffer = CachedBufferer.partialFacing(partial, blockState);
		if (rotate) {
			superBuffer.rotateCentered(Direction.UP, AngleHelper.rad(90));
		}
		superBuffer.color(0xFFFFFF)
			.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
	}

	protected void renderShaft(SawTileEntity te, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		KineticTileEntityRenderer.renderRotatingBuffer(te, getRotatedModel(te), ms,
			buffer.getBuffer(RenderType.solid()), light);
	}

	protected void renderItems(SawTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		boolean processingMode = te.getBlockState()
			.getValue(SawBlock.FACING) == Direction.UP;
		if (processingMode && !te.inventory.isEmpty()) {
			boolean alongZ = !te.getBlockState()
				.getValue(SawBlock.AXIS_ALONG_FIRST_COORDINATE);
			ms.pushPose();

			boolean moving = te.inventory.recipeDuration != 0;
			float offset = moving ? (float) (te.inventory.remainingTime) / te.inventory.recipeDuration : 0;
			float processingSpeed = Mth.clamp(Math.abs(te.getSpeed()) / 32, 1, 128);
			if (moving) {
				offset = Mth
					.clamp(offset + ((-partialTicks + .5f) * processingSpeed) / te.inventory.recipeDuration, 0.125f, 1f);
				if (!te.inventory.appliedRecipe)
					offset += 1;
				offset /= 2;
			}

			if (te.getSpeed() == 0)
				offset = .5f;
			if (te.getSpeed() < 0 ^ alongZ)
				offset = 1 - offset;

			for (int i = 0; i < te.inventory.getSlots(); i++) {
				ItemStack stack = te.inventory.getStackInSlot(i);
				if (stack.isEmpty())
					continue;

				ItemRenderer itemRenderer = Minecraft.getInstance()
					.getItemRenderer();
				BakedModel modelWithOverrides = itemRenderer.getModel(stack, te.getLevel(), null, 0);
				boolean blockItem = modelWithOverrides.isGui3d();

				ms.translate(alongZ ? offset : .5, blockItem ? .925f : 13f / 16f, alongZ ? .5 : offset);

				ms.scale(.5f, .5f, .5f);
				if (alongZ)
					ms.mulPose(Vector3f.YP.rotationDegrees(90));
				ms.mulPose(Vector3f.XP.rotationDegrees(90));
				itemRenderer.renderStatic(stack, ItemTransforms.TransformType.FIXED, light, overlay, ms, buffer, 0);
				break;
			}

			ms.popPose();
		}
	}

	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		BlockState state = te.getBlockState();
		if (state.getValue(FACING)
			.getAxis()
			.isHorizontal())
			return CachedBufferer.partialFacing(AllBlockPartials.SHAFT_HALF,
				state.rotate(te.getLevel(), te.getBlockPos(), Rotation.CLOCKWISE_180));
		return CachedBufferer.block(KineticTileEntityRenderer.KINETIC_TILE,
			getRenderedBlockState(te));
	}

	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return KineticTileEntityRenderer.shaft(KineticTileEntityRenderer.getRotationAxisOf(te));
	}

	public static void renderInContraption(MovementContext context, PlacementSimulationWorld renderWorld,
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
				superBuffer = CachedBufferer.partial(AllBlockPartials.SAW_BLADE_HORIZONTAL_ACTIVE, state);
			else
				superBuffer = CachedBufferer.partial(AllBlockPartials.SAW_BLADE_HORIZONTAL_INACTIVE, state);
		} else {
			if (shouldAnimate)
				superBuffer = CachedBufferer.partial(AllBlockPartials.SAW_BLADE_VERTICAL_ACTIVE, state);
			else
				superBuffer = CachedBufferer.partial(AllBlockPartials.SAW_BLADE_VERTICAL_INACTIVE, state);
		}

		superBuffer.transform(matrices.getModel())
			.centre()
			.rotateY(AngleHelper.horizontalAngle(facing))
			.rotateX(AngleHelper.verticalAngle(facing));

		if (!SawBlock.isHorizontal(state)) {
			superBuffer.rotateZ(state.getValue(SawBlock.AXIS_ALONG_FIRST_COORDINATE) ? 90 : 0);
		}

		superBuffer.unCentre()
			.light(matrices.getWorld(), ContraptionRenderDispatcher.getContraptionWorldLight(context, renderWorld))
			.renderInto(matrices.getViewProjection(), buffer.getBuffer(RenderType.cutoutMipped()));
	}

}
