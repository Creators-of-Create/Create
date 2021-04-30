package com.simibubi.create.content.logistics.block.funnel;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.backend.FastRenderDispatcher;
import com.simibubi.create.foundation.render.backend.core.PartialModel;
import com.simibubi.create.foundation.tileEntity.renderer.SmartTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class FunnelRenderer extends SmartTileEntityRenderer<FunnelTileEntity> {

	public FunnelRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(FunnelTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);

		if (!te.hasFlap() || FastRenderDispatcher.available(te.getWorld()))
			return;

		BlockState blockState = te.getBlockState();
		IVertexBuilder vb = buffer.getBuffer(RenderType.getSolid());
		PartialModel partialModel = (blockState.getBlock() instanceof FunnelBlock ? AllBlockPartials.FUNNEL_FLAP
				: AllBlockPartials.BELT_FUNNEL_FLAP);
		SuperByteBuffer flapBuffer = PartialBufferer.get(partialModel, blockState);
		Vector3d pivot = VecHelper.voxelSpace(0, 10, 9.5f);
		MatrixStacker msr = MatrixStacker.of(ms);

		float horizontalAngle = AngleHelper.horizontalAngle(FunnelBlock.getFunnelFacing(blockState)
				.getOpposite());
		float f = te.flap.get(partialTicks);

		ms.push();
		msr.centre()
				.rotateY(horizontalAngle)
			.unCentre();
		ms.translate(0, 0, -te.getFlapOffset());

		for (int segment = 0; segment <= 3; segment++) {
			ms.push();

			float intensity = segment == 3 ? 1.5f : segment + 1;
			float abs = Math.abs(f);
			float flapAngle = MathHelper.sin((float) ((1 - abs) * Math.PI * intensity)) * 30 * -f;
			if (f > 0)
				flapAngle *= .5f;

			msr.translate(pivot)
				.rotateX(flapAngle)
				.translateBack(pivot);

			flapBuffer.light(light)
				.renderInto(ms, vb);

			ms.pop();
			ms.translate(-3 / 16f, 0, 0);
		}
		ms.pop();
	}

}
