package com.simibubi.create.content.optics.mirror;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;

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

		// if (FastRenderDispatcher.available(te.getWorld())) return;
		MirrorTileEntity mirrorTe = (MirrorTileEntity) te;

		renderMirror(mirrorTe, partialTicks, ms, buffer, light);
		((MirrorTileEntity) te).getRenderBeams()
				.forEachRemaining(beam -> beam.render(ms, buffer, partialTicks));
	}

	private void renderMirror(MirrorTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer, int light) {

		final Direction.Axis facing = te.getBlockState()
				.get(BlockStateProperties.AXIS);
		SuperByteBuffer superBuffer = AllBlockPartials.MIRROR_PLANE.renderOn(te.getBlockState());

		float interpolatedAngle = te.getInterpolatedAngle(partialTicks - 1);
		kineticRotationTransform(superBuffer, te, facing, (float) (interpolatedAngle / 180 * Math.PI), light);

		switch (facing) {
			case X:
				superBuffer.rotateCentered(Direction.UP, AngleHelper.rad(90));
				break;
			case Y:
				superBuffer.rotateCentered(Direction.EAST, AngleHelper.rad(90));
				break;
			default:
				superBuffer.rotateCentered(Direction.UP, AngleHelper.rad(180));
		}

		superBuffer.renderInto(ms, buffer.getBuffer(RenderType.getSolid()));
	}

	@Override
	public boolean isGlobalRenderer(KineticTileEntity tileEntity) {
		return true;
	}
}
