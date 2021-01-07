package com.simibubi.create.content.contraptions.components.fan;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;

import com.simibubi.create.foundation.utility.render.instancing.RotatingBuffer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class EncasedFanRenderer extends KineticTileEntityRenderer {

	public EncasedFanRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		Direction direction = te.getBlockState()
			.get(FACING);

		int lightBehind = WorldRenderer.getLightmapCoordinates(te.getWorld(), te.getPos().offset(direction.getOpposite()));
		int lightInFront = WorldRenderer.getLightmapCoordinates(te.getWorld(), te.getPos().offset(direction));
		
		RotatingBuffer shaftHalf =
			AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouthRotating(te.getBlockState(), direction.getOpposite());
		RotatingBuffer fanInner =
			AllBlockPartials.ENCASED_FAN_INNER.renderOnDirectionalSouthRotating(te.getBlockState(), direction.getOpposite());

		renderRotatingBuffer(te, shaftHalf, lightBehind);
		fanInner.setupInstance(data -> {
			final BlockPos pos = te.getPos();
			Direction.Axis axis = ((IRotate) te.getBlockState()
											   .getBlock()).getRotationAxis(te.getBlockState());

			float speed = te.getSpeed() * 5;
			if (speed > 0)
				speed = MathHelper.clamp(speed, 80, 64 * 20);
			if (speed < 0)
				speed = MathHelper.clamp(speed, -64 * 20, -80);

			data.setPackedLight(lightInFront)
				.setRotationalSpeed(speed)
				.setRotationOffset(getRotationOffsetForPosition(te, pos, axis))
				.setRotationAxis(Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, axis).getUnitVector())
				.setPosition(pos);
		});
	}

}
