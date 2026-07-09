package com.simibubi.create.content.fluids.pipes.valve;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.createmod.catnip.api.math.AngleHelper;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class FluidValveRenderer extends KineticBlockEntityRenderer<FluidValveBlockEntity> {

	public FluidValveRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(FluidValveBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {

		if (VisualizationManager.supportsVisualization(be.getLevel())) return;

		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
		BlockState blockState = be.getBlockState();
		SuperByteBuffer pointer = CachedBuffers.partial(AllPartialModels.FLUID_VALVE_POINTER, blockState);
		Direction facing = blockState.getValue(FluidValveBlock.FACING);

		float pointerRotation = Mth.lerp(be.pointer.getValue(partialTicks), 0, -90);
		Axis pipeAxis = FluidValveBlock.getPipeAxis(blockState);
		Axis shaftAxis = getRotationAxisOf(be);

		int pointerRotationOffset = 0;
		if (pipeAxis.isHorizontal() && shaftAxis == Axis.X || pipeAxis.isVertical())
			pointerRotationOffset = 90;

		pointer.center()
			.rotateYDegrees(AngleHelper.horizontalAngle(facing))
			.rotateXDegrees(facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90)
			.rotateYDegrees(pointerRotationOffset + pointerRotation)
			.uncenter()
			.light(light)
			.renderInto(ms, buffer.getBuffer(com.simibubi.create.foundation.render.LegacyRenderTypes.solid()));
	}

	@Override
	protected BlockState getRenderedBlockState(FluidValveBlockEntity be) {
		return shaft(getRotationAxisOf(be));
	}

}
