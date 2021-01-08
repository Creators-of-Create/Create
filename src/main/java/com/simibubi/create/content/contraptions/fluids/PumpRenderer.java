package com.simibubi.create.content.contraptions.fluids;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.render.instancing.InstanceBuffer;
import com.simibubi.create.foundation.utility.render.SuperByteBuffer;

import com.simibubi.create.foundation.utility.render.instancing.RotatingBuffer;
import com.simibubi.create.foundation.utility.render.instancing.RotatingData;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PumpRenderer extends KineticTileEntityRenderer {

	public PumpRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
		if (!(te instanceof PumpTileEntity))
			return;
		PumpTileEntity pump = (PumpTileEntity) te;
		Vec3d rotationOffset = new Vec3d(.5, 14 / 16f, .5);
		BlockState blockState = te.getBlockState();
		float angle = MathHelper.lerp(pump.arrowDirection.getValue(partialTicks), 0, 90) - 90;
		for (float yRot : new float[] { 0, 90 }) {
			ms.push();
			SuperByteBuffer arrow = AllBlockPartials.MECHANICAL_PUMP_ARROW.renderOn(blockState);
			Direction direction = blockState.get(PumpBlock.FACING);
			MatrixStacker.of(ms)
				.centre()
				.rotateY(AngleHelper.horizontalAngle(direction) + 180)
				.rotateX(-AngleHelper.verticalAngle(direction) - 90)
				.unCentre()
				.translate(rotationOffset)
				.rotateY(yRot)
				.rotateZ(angle)
				.translateBack(rotationOffset);
			arrow.light(light).renderInto(ms, buffer.getBuffer(RenderType.getSolid()));
			ms.pop();
		}
	}

	@Override
	protected RotatingBuffer getRotatedModel(KineticTileEntity te) {
		return AllBlockPartials.MECHANICAL_PUMP_COG.renderOnDirectionalSouthRotating(te.getBlockState());
	}

}
