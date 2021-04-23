package com.simibubi.create.content.optics.mirror;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.backend.FastRenderDispatcher;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;

@ParametersAreNonnullByDefault
public class MirrorRenderer extends KineticTileEntityRenderer {
	public MirrorRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
							  int light, int overlay) {

		if (!FastRenderDispatcher.available(te.getWorld()))
			renderMirror((MirrorTileEntity) te, partialTicks, ms, buffer, light);
		((MirrorTileEntity) te).getHandler()
				.getRenderBeams()
				.forEachRemaining(beam -> beam.render(ms, buffer, partialTicks));
	}

	private void renderMirror(MirrorTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer, int light) {

		final Direction.Axis facing = te.getBlockState()
				.get(BlockStateProperties.AXIS);
		SuperByteBuffer superBuffer = AllBlockPartials.MIRROR_PLANE.renderOnDirectionalSouth(te.getBlockState(), te.getHandler()
				.getMirrorAxis());

		float interpolatedAngle = te.getHandler()
				.getInterpolatedAngle(partialTicks - 1);
		kineticRotationTransform(superBuffer, te, facing, (float) (interpolatedAngle / 180 * Math.PI), light);
		superBuffer.renderInto(ms, buffer.getBuffer(RenderType.getTranslucent()));
	}

	@Override
	public boolean isGlobalRenderer(KineticTileEntity tileEntity) {
		return true;
	}
}
