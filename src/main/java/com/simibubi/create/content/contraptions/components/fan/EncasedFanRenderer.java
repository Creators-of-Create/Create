package com.simibubi.create.content.contraptions.components.fan;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;

import com.simibubi.create.foundation.utility.render.instancing.InstanceBuffer;
import com.simibubi.create.foundation.utility.render.instancing.InstanceContext;
import com.simibubi.create.foundation.utility.render.instancing.RotatingData;
import net.minecraft.client.renderer.IRenderTypeBuffer;
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
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
	}

	@Override
	public void addInstanceData(InstanceContext<KineticTileEntity> ctx) {
		KineticTileEntity te = ctx.te;
		Direction direction = te.getBlockState()
								.get(FACING);

		InstanceBuffer<RotatingData> shaftHalf =
				AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouthRotating(ctx, te.getBlockState(), direction.getOpposite());
		InstanceBuffer<RotatingData> fanInner =
				AllBlockPartials.ENCASED_FAN_INNER.renderOnDirectionalSouthRotating(ctx, te.getBlockState(), direction.getOpposite());

		renderRotatingBuffer(ctx, shaftHalf);
		fanInner.setupInstance(data -> {
			final BlockPos pos = te.getPos();
			Direction.Axis axis = ((IRotate) te.getBlockState()
											   .getBlock()).getRotationAxis(te.getBlockState());

			float speed = te.getSpeed() * 5;
			if (speed > 0)
				speed = MathHelper.clamp(speed, 80, 64 * 20);
			if (speed < 0)
				speed = MathHelper.clamp(speed, -64 * 20, -80);

			data.setRotationalSpeed(speed)
				.setRotationOffset(getRotationOffsetForPosition(te, pos, axis))
				.setRotationAxis(Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, axis).getUnitVector())
				.setTileEntity(te);

			if (ctx.checkWorldLight()) {
				BlockPos inFront = te.getPos().offset(direction);
				int blockLight = te.getWorld().getLightLevel(LightType.BLOCK, inFront);
				int skyLight = te.getWorld().getLightLevel(LightType.SKY, inFront);

				data.setBlockLight(blockLight)
					.setSkyLight(skyLight);
			}
		});
	}
}
