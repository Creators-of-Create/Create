package com.simibubi.create.content.optics.mirror;

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

public class MirrorRenderer extends KineticTileEntityRenderer {
	public MirrorRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
							  int light, int overlay) {

		// if (FastRenderDispatcher.available(te.getWorld())) return;

		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);

		MirrorTileEntity bearingTe = (MirrorTileEntity) te;
		final Direction.Axis facing = te.getBlockState()
				.get(BlockStateProperties.AXIS);
		SuperByteBuffer superBuffer = AllBlockPartials.MIRROR_PLANE.renderOn(te.getBlockState());

		float interpolatedAngle = bearingTe.getInterpolatedAngle(partialTicks - 1);
		kineticRotationTransform(superBuffer, te, facing, (float) (interpolatedAngle / 180 * Math.PI), light);

		if (facing == Direction.Axis.X) {
			superBuffer.rotateCentered(Direction.UP, AngleHelper.rad(90));
		}
		if (facing == Direction.Axis.Y) {
			superBuffer.rotateCentered(Direction.EAST, AngleHelper.rad(90));
		}
		if (facing == Direction.Axis.Z) {
			superBuffer.rotateCentered(Direction.UP, AngleHelper.rad(180));
		}
		superBuffer.renderInto(ms, buffer.getBuffer(RenderType.getSolid()));
	}
}
