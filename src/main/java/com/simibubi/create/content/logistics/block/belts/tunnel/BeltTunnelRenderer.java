package com.simibubi.create.content.logistics.block.belts.tunnel;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.backend.FastRenderDispatcher;
import com.simibubi.create.foundation.tileEntity.renderer.SmartTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class BeltTunnelRenderer extends SmartTileEntityRenderer<BeltTunnelTileEntity> {

	public BeltTunnelRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(BeltTunnelTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);

		if (FastRenderDispatcher.available(te.getWorld())) return;

		SuperByteBuffer flapBuffer = AllBlockPartials.BELT_TUNNEL_FLAP.renderOn(te.getBlockState());
		IVertexBuilder vb = buffer.getBuffer(RenderType.getSolid());
		Vector3d pivot = VecHelper.voxelSpace(0, 10, 1f);
		MatrixStacker msr = MatrixStacker.of(ms);

		for (Direction direction : Iterate.directions) {
			if (!te.flaps.containsKey(direction))
				continue;

			float horizontalAngle = AngleHelper.horizontalAngle(direction.getOpposite());
			float f = te.flaps.get(direction)
				.get(partialTicks);

			ms.push();
			msr.centre()
				.rotateY(horizontalAngle)
				.unCentre();

			for (int segment = 0; segment <= 3; segment++) {
				ms.push();
				float intensity = segment == 3 ? 1.5f : segment + 1;
				float abs = Math.abs(f);
				float flapAngle = MathHelper.sin((float) ((1 - abs) * Math.PI * intensity)) * 30 * f
					* (direction.getAxis() == Axis.X ? 1 : -1);
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

}
