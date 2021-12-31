package com.simibubi.create.content.logistics.block.belts.tunnel;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.renderer.SmartTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class BeltTunnelRenderer extends SmartTileEntityRenderer<BeltTunnelTileEntity> {

	public BeltTunnelRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(BeltTunnelTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);

		if (Backend.canUseInstancing(te.getLevel())) return;

		SuperByteBuffer flapBuffer = CachedBufferer.partial(AllBlockPartials.BELT_TUNNEL_FLAP, te.getBlockState());
		VertexConsumer vb = buffer.getBuffer(RenderType.solid());
		Vec3 pivot = VecHelper.voxelSpace(0, 10, 1f);
		TransformStack msr = TransformStack.cast(ms);

		for (Direction direction : Iterate.directions) {
			if (!te.flaps.containsKey(direction))
				continue;

			float horizontalAngle = AngleHelper.horizontalAngle(direction.getOpposite());
			float f = te.flaps.get(direction)
				.get(partialTicks);

			ms.pushPose();
			msr.centre()
				.rotateY(horizontalAngle)
				.unCentre();

			for (int segment = 0; segment <= 3; segment++) {
				ms.pushPose();
				float intensity = segment == 3 ? 1.5f : segment + 1;
				float abs = Math.abs(f);
				float flapAngle = Mth.sin((float) ((1 - abs) * Math.PI * intensity)) * 30 * f
					* (direction.getAxis() == Axis.X ? 1 : -1);
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

}
