package com.simibubi.create.modules.contraptions.components.fan;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.MathHelper;

public class EncasedFanTileEntityRenderer extends KineticTileEntityRenderer {

	@Override
	public void renderTileEntityFast(KineticTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		super.renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, buffer);

		float time = AnimationTickHolder.getRenderTick();
		float speed = te.getSpeed() * 20;
		if (speed > 0)
			speed = MathHelper.clamp(speed, 80, 64 * 20);
		if (speed < 0)
			speed = MathHelper.clamp(speed, -64 * 20, -80);
		float angle = (time * speed) % 360;
		angle = angle / 180f * (float) Math.PI;

		SuperByteBuffer superByteBuffer = CreateClient.bufferCache.renderBlockState(KINETIC_TILE,
				getRenderedPropellerState(te));
		kineticRotationTransform(superByteBuffer, te, te.getBlockState().get(FACING).getAxis(), angle, getWorld());
		superByteBuffer.translate(x, y, z).renderInto(buffer);

	}

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return AllBlocks.SHAFT_HALF.get().getDefaultState().with(FACING, te.getBlockState().get(FACING).getOpposite());
	}

	protected BlockState getRenderedPropellerState(KineticTileEntity te) {
		return AllBlocks.ENCASED_FAN_INNER.get().getDefaultState().with(FACING, te.getBlockState().get(FACING));
	}

}
