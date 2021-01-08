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
import net.minecraft.world.LightType;

public class EncasedFanRenderer extends KineticTileEntityRenderer {

	public EncasedFanRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		addInstanceData(te);
	}

	@Override
	public void addInstanceData(KineticTileEntity te) {
		Direction direction = te.getBlockState()
								.get(FACING);

		BlockPos inFront = te.getPos().offset(direction);
		int blockLight = te.getWorld().getLightLevel(LightType.BLOCK, inFront);
		int skyLight = te.getWorld().getLightLevel(LightType.SKY, inFront);

		RotatingBuffer shaftHalf =
				AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouthRotating(te.getBlockState(), direction.getOpposite());
		RotatingBuffer fanInner =
				AllBlockPartials.ENCASED_FAN_INNER.renderOnDirectionalSouthRotating(te.getBlockState(), direction.getOpposite());

		renderRotatingBuffer(te, shaftHalf);
		fanInner.setupInstance(data -> {
			final BlockPos pos = te.getPos();
			Direction.Axis axis = ((IRotate) te.getBlockState()
											   .getBlock()).getRotationAxis(te.getBlockState());

			float speed = te.getSpeed() * 5;
			if (speed > 0)
				speed = MathHelper.clamp(speed, 80, 64 * 20);
			if (speed < 0)
				speed = MathHelper.clamp(speed, -64 * 20, -80);

			data.setBlockLight(blockLight)
				.setSkyLight(skyLight)
				.setRotationalSpeed(speed)
				.setRotationOffset(getRotationOffsetForPosition(te, pos, axis))
				.setRotationAxis(Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, axis).getUnitVector())
				.setPosition(pos);
		});
	}
}
