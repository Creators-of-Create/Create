package com.simibubi.create.content.contraptions.components.actors.controls;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.tileEntity.renderer.SmartTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ContraptionControlsRenderer extends SmartTileEntityRenderer<ContraptionControlsTileEntity> {

	public ContraptionControlsRenderer(Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(ContraptionControlsTileEntity tileEntityIn, float pt, PoseStack ms,
		MultiBufferSource buffer, int light, int overlay) {
		BlockState blockState = tileEntityIn.getBlockState();
		Direction facing = blockState.getValue(ContraptionControlsBlock.FACING)
			.getOpposite();
		Vec3 buttonAxis = VecHelper.rotate(new Vec3(0, 1, -.325), AngleHelper.horizontalAngle(facing), Axis.Y)
			.scale(-1 / 24f * tileEntityIn.button.getValue(pt));

		ms.pushPose();
		ms.translate(buttonAxis.x, buttonAxis.y, buttonAxis.z);
		super.renderSafe(tileEntityIn, pt, ms, buffer, light, overlay);
		
		VertexConsumer vc = buffer.getBuffer(RenderType.solid());
		CachedBufferer.partialFacing(AllBlockPartials.CONTRAPTION_CONTROLS_BUTTON, blockState, facing)
			.light(light)
			.renderInto(ms, vc);
		
		ms.popPose();

		int i = (((int) tileEntityIn.indicator.getValue(pt) / 45) % 8) + 8;
		CachedBufferer.partialFacing(AllBlockPartials.CONTRAPTION_CONTROLS_INDICATOR.get(i % 8), blockState, facing)
			.light(light)
			.renderInto(ms, vc);

	}

}