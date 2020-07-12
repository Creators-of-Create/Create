package com.simibubi.create.content.contraptions.processing;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.SuperByteBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;

public class HeaterRenderer extends SafeTileEntityRenderer<HeaterTileEntity> {
	private static final Minecraft INSTANCE = Minecraft.getInstance();

	public HeaterRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(HeaterTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		AllBlockPartials blazeModel;
		switch (te.getHeatLevel()) {
		case 2:
			blazeModel = AllBlockPartials.BLAZE_HEATER_BLAZE_TWO;
			break;
		case 3:
			blazeModel = AllBlockPartials.BLAZE_HEATER_BLAZE_THREE;
			break;
		case 4:
			blazeModel = AllBlockPartials.BLAZE_HEATER_BLAZE_FOUR;
			break;
		default:
			blazeModel = AllBlockPartials.BLAZE_HEATER_BLAZE_ONE;
		}
		Vector3f difference = new Vector3f(INSTANCE.player.getPositionVector()
			.subtract(te.getPos()
				.getX() + 0.5, 0,
				te.getPos()
					.getZ() + 0.5)
			.mul(1, 0, 1));
		difference.normalize();

		SuperByteBuffer blazeBuffer = blazeModel.renderOn(te.getBlockState());
		blazeBuffer.rotateCentered(Direction.UP,
			(float) ((difference.getX() < 0 ? 1 : -1) * Math.acos(Direction.NORTH.getUnitVector()
				.dot(difference))));
		blazeBuffer.renderInto(ms, buffer.getBuffer(RenderType.getSolid()));
	}
}
