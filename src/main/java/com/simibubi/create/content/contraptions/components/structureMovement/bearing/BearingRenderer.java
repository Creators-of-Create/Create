package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.render.SuperByteBuffer;

import com.simibubi.create.foundation.utility.render.instancing.InstanceBuffer;
import com.simibubi.create.foundation.utility.render.instancing.InstanceContext;
import com.simibubi.create.foundation.utility.render.instancing.RotatingData;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;

public class BearingRenderer extends KineticTileEntityRenderer {

	public BearingRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);

		IBearingTileEntity bearingTe = (IBearingTileEntity) te;
		final Direction facing = te.getBlockState()
			.get(BlockStateProperties.FACING);
		AllBlockPartials top =
			bearingTe.isWoodenTop() ? AllBlockPartials.BEARING_TOP_WOODEN : AllBlockPartials.BEARING_TOP;
		SuperByteBuffer superBuffer = top.renderOn(te.getBlockState());

		float interpolatedAngle = bearingTe.getInterpolatedAngle(partialTicks - 1);
		kineticRotationTransform(superBuffer, te, facing.getAxis(), (float) (interpolatedAngle / 180 * Math.PI), light);

		if (facing.getAxis()
			.isHorizontal())
			superBuffer.rotateCentered(Direction.UP,
				AngleHelper.rad(AngleHelper.horizontalAngle(facing.getOpposite())));
		superBuffer.rotateCentered(Direction.EAST, AngleHelper.rad(-90 - AngleHelper.verticalAngle(facing)));
		superBuffer.renderInto(ms, buffer.getBuffer(RenderType.getSolid()));
	}

	@Override
	protected InstanceBuffer<RotatingData> getRotatedModel(InstanceContext<? extends KineticTileEntity> ctx) {
		BlockState blockState = ctx.te.getBlockState();
		return AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouthRotating(ctx, blockState, blockState.get(BearingBlock.FACING).getOpposite());
	}

}
