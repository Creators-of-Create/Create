package com.simibubi.create.modules.contraptions.components.fan;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;

public class EncasedFanTileEntityRenderer extends KineticTileEntityRenderer {

	public EncasedFanTileEntityRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
			int light, int overlay) {
		Direction direction = te.getBlockState().get(FACING);
		SuperByteBuffer superBuffer = AllBlockPartials.SHAFT_HALF.renderOnDirectional(te.getBlockState(),
				direction.getOpposite());
		standardKineticRotationTransform(superBuffer, te, getWorld()).translate(x, y, z).renderInto(buffer);

		float time = AnimationTickHolder.getRenderTick();
		float speed = te.getSpeed() * 5;
		if (speed > 0)
			speed = MathHelper.clamp(speed, 80, 64 * 20);
		if (speed < 0)
			speed = MathHelper.clamp(speed, -64 * 20, -80);
		float angle = (time * speed * 3 / 10f) % 360;
		angle = angle / 180f * (float) Math.PI;

		SuperByteBuffer superByteBuffer = AllBlockPartials.ENCASED_FAN_INNER.renderOnDirectional(te.getBlockState(),
				direction.getOpposite());
		kineticRotationTransform(superByteBuffer, te, direction.getAxis(), angle, getWorld());
		superByteBuffer.translate(x, y, z).renderInto(buffer);
	}

}
