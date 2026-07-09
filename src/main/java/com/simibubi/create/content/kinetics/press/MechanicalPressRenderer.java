package com.simibubi.create.content.kinetics.press;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class MechanicalPressRenderer extends KineticBlockEntityRenderer<MechanicalPressBlockEntity> {

	public MechanicalPressRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	public boolean shouldRenderOffScreen(MechanicalPressBlockEntity be) {
		return true;
	}

	@Override
	protected void renderSafe(MechanicalPressBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

		if (VisualizationManager.supportsVisualization(be.getLevel()))
			return;

		BlockState blockState = be.getBlockState();
		PressingBehaviour pressingBehaviour = be.getPressingBehaviour();
		float renderedHeadOffset =
			pressingBehaviour.getRenderedHeadOffset(partialTicks) * pressingBehaviour.mode.headOffset;

		SuperByteBuffer headRender = CachedBuffers.partialFacing(AllPartialModels.MECHANICAL_PRESS_HEAD, blockState,
			blockState.getValue(HORIZONTAL_FACING));
		headRender.translate(0, -renderedHeadOffset, 0)
			.light(light)
			.renderInto(ms, buffer.getBuffer(com.simibubi.create.foundation.render.LegacyRenderTypes.solid()));
	}

	@Override
	protected BlockState getRenderedBlockState(MechanicalPressBlockEntity be) {
		return shaft(getRotationAxisOf(be));
	}

}
