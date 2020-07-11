package com.simibubi.create.content.logistics.block.funnel;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.tileEntity.renderer.SmartTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class FunnelRenderer extends SmartTileEntityRenderer<FunnelTileEntity> {

	public FunnelRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(FunnelTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);

		if (!te.hasFlap())
			return;

		IVertexBuilder vb = buffer.getBuffer(RenderType.getSolid());
		SuperByteBuffer flapBuffer = AllBlockPartials.BELT_FUNNEL_FLAP.renderOn(te.getBlockState());
		Vec3d pivot = VecHelper.voxelSpace(0, 10, 9.5f);
		MatrixStacker msr = MatrixStacker.of(ms);

		float horizontalAngle = AngleHelper.horizontalAngle(FunnelBlock.getFunnelFacing(te.getBlockState())
			.getOpposite());
		float f = te.flap.get(partialTicks);

		ms.push();
		msr.centre()
			.rotateY(horizontalAngle)
			.unCentre();
		
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
