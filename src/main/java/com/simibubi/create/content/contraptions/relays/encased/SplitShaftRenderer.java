package com.simibubi.create.content.contraptions.relays.encased;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;

public class SplitShaftRenderer extends KineticTileEntityRenderer {

	public SplitShaftRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
			int light, int overlay) {
		if (Backend.getInstance().canUseInstancing(te.getLevel())) return;

		Block block = te.getBlockState().getBlock();
		final Axis boxAxis = ((IRotate) block).getRotationAxis(te.getBlockState());
		final BlockPos pos = te.getBlockPos();
		float time = AnimationTickHolder.getRenderTime(te.getLevel());

		for (Direction direction : Iterate.directions) {
			Axis axis = direction.getAxis();
			if (boxAxis != axis)
				continue;

			float offset = getRotationOffsetForPosition(te, pos, axis);
			float angle = (time * te.getSpeed() * 3f / 10) % 360;
			float modifier = 1;

			if (te instanceof SplitShaftTileEntity)
				modifier = ((SplitShaftTileEntity) te).getRotationSpeedModifier(direction);

			angle *= modifier;
			angle += offset;
			angle = angle / 180f * (float) Math.PI;

			SuperByteBuffer superByteBuffer =
					PartialBufferer.getFacing(AllBlockPartials.SHAFT_HALF, te.getBlockState(), direction);
			kineticRotationTransform(superByteBuffer, te, axis, angle, light);
			superByteBuffer.renderInto(ms, buffer.getBuffer(RenderType.solid()));
		}
	}

}
