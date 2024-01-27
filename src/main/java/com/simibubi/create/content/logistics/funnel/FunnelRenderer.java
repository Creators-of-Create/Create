package com.simibubi.create.content.logistics.funnel;

import com.jozufozu.flywheel.api.visualization.VisualizationManager;
import com.jozufozu.flywheel.lib.model.baked.PartialModel;
import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FunnelRenderer extends SmartBlockEntityRenderer<FunnelBlockEntity> {

	public FunnelRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(FunnelBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

		if (!be.hasFlap() || VisualizationManager.supportsVisualization(be.getLevel()))
			return;

		BlockState blockState = be.getBlockState();
		VertexConsumer vb = buffer.getBuffer(RenderType.solid());
		PartialModel partialModel = (blockState.getBlock() instanceof FunnelBlock ? AllPartialModels.FUNNEL_FLAP
			: AllPartialModels.BELT_FUNNEL_FLAP);
		SuperByteBuffer flapBuffer = CachedBufferer.partial(partialModel, blockState);
		Vec3 pivot = VecHelper.voxelSpace(0, 10, 9.5f);
		var msr = TransformStack.of(ms);

		float horizontalAngle = AngleHelper.horizontalAngle(FunnelBlock.getFunnelFacing(blockState)
			.getOpposite());
		float f = be.flap.getValue(partialTicks);

		ms.pushPose();
		msr.center()
			.rotateYDegrees(horizontalAngle)
			.uncenter();
		ms.translate(0.075f / 16f, 0, -be.getFlapOffset());

		for (int segment = 0; segment <= 3; segment++) {
			ms.pushPose();

			float intensity = segment == 3 ? 1.5f : segment + 1;
			float abs = Math.abs(f);
			float flapAngle = Mth.sin((float) ((1 - abs) * Math.PI * intensity)) * 30 * -f;
			if (f > 0)
				flapAngle *= .5f;

			msr.translate(pivot)
				.rotateXDegrees(flapAngle)
				.translateBack(pivot);

			flapBuffer.light(light)
				.renderInto(ms, vb);

			ms.popPose();
			ms.translate(-3.05f / 16f, 0, 0);
		}
		ms.popPose();
	}

}
