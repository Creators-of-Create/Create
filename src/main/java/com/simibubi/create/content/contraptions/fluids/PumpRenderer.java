package com.simibubi.create.content.contraptions.fluids;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PumpRenderer extends KineticTileEntityRenderer {

	public PumpRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
		if (!(te instanceof PumpTileEntity pump))
			return;
		Vec3 rotationOffset = new Vec3(.5, 14 / 16f, .5);
		BlockState blockState = te.getBlockState();
		float angle = Mth.lerp(pump.arrowDirection.getValue(partialTicks), 0, 90) - 90;
		SuperByteBuffer arrow = CachedBufferer.partial(AllBlockPartials.MECHANICAL_PUMP_ARROW, blockState);
		for (float yRot : new float[] { 0, 90 }) {
			Direction direction = blockState.getValue(PumpBlock.FACING);
            arrow.centre()
					.rotateY(AngleHelper.horizontalAngle(direction) + 180)
					.rotateX(-AngleHelper.verticalAngle(direction) - 90)
					.unCentre()
					.translate(rotationOffset)
					.rotateY(yRot)
					.rotateZ(angle)
					.translateBack(rotationOffset)
					.light(light)
					.renderInto(ms, buffer.getBuffer(RenderType.solid()));
		}
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return CachedBufferer.partialFacing(AllBlockPartials.MECHANICAL_PUMP_COG, te.getBlockState());
	}

}
