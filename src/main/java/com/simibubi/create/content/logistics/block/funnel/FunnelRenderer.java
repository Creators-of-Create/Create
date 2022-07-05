package com.simibubi.create.content.logistics.block.funnel;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.renderer.SmartTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FunnelRenderer extends SmartTileEntityRenderer<FunnelTileEntity> {

	public FunnelRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(FunnelTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);

		if (!te.hasFlap() || Backend.canUseInstancing(te.getLevel()))
			return;

		BlockState blockState = te.getBlockState();
		VertexConsumer vb = buffer.getBuffer(RenderType.solid());
		PartialModel partialModel = (blockState.getBlock() instanceof FunnelBlock ? AllBlockPartials.FUNNEL_FLAP
			: AllBlockPartials.BELT_FUNNEL_FLAP);
		SuperByteBuffer flapBuffer = CachedBufferer.partial(partialModel, blockState);
		Vec3 pivot = VecHelper.voxelSpace(0, 10, 9.5f);
		TransformStack msr = TransformStack.cast(ms);

		float horizontalAngle = AngleHelper.horizontalAngle(FunnelBlock.getFunnelFacing(blockState)
			.getOpposite());
		float f = te.flap.getValue(partialTicks);

		ms.pushPose();
		msr.centre()
			.rotateY(horizontalAngle)
			.unCentre();
		ms.translate(0, 0, -te.getFlapOffset());

		for (int segment = 0; segment <= 3; segment++) {
			ms.pushPose();

			float intensity = segment == 3 ? 1.5f : segment + 1;
			float abs = Math.abs(f);
			float flapAngle = Mth.sin((float) ((1 - abs) * Math.PI * intensity)) * 30 * -f;
			if (f > 0)
				flapAngle *= .5f;

			msr.translate(pivot)
				.rotateX(flapAngle)
				.translateBack(pivot);

			flapBuffer.light(light)
				.renderInto(ms, vb);

			ms.popPose();
			ms.translate(-3 / 16f, 0, 0);
		}
		ms.popPose();
	}

}
