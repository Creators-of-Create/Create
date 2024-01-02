package com.simibubi.create.content.kinetics.crank;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

import com.jozufozu.flywheel.api.visualization.VisualizationManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

public class HandCrankRenderer extends KineticBlockEntityRenderer<HandCrankBlockEntity> {

	public HandCrankRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(HandCrankBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		if (be.shouldRenderShaft())
			super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

		if (VisualizationManager.supportsVisualization(be.getLevel()))
			return;

		Direction facing = be.getBlockState()
			.getValue(FACING);
		kineticRotationTransform(be.getRenderedHandle(), be, facing.getAxis(), be.getIndependentAngle(partialTicks),
			light).renderInto(ms, buffer.getBuffer(RenderType.solid()));
	}

}
