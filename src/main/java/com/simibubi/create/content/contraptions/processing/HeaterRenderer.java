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

import java.util.HashMap;

public class HeaterRenderer extends SafeTileEntityRenderer<HeaterTileEntity> {
	private static final Minecraft INSTANCE = Minecraft.getInstance();
	private static final HashMap<HeaterBlock.HeatLevel, AllBlockPartials> blazeModelMap = new HashMap<>();

	public HeaterRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
		blazeModelMap.put(HeaterBlock.HeatLevel.FADING, AllBlockPartials.BLAZE_HEATER_BLAZE_TWO);
		blazeModelMap.put(HeaterBlock.HeatLevel.KINDLED, AllBlockPartials.BLAZE_HEATER_BLAZE_THREE);
		blazeModelMap.put(HeaterBlock.HeatLevel.SEETHING, AllBlockPartials.BLAZE_HEATER_BLAZE_FOUR);
	}

	@Override
	protected void renderSafe(HeaterTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		AllBlockPartials blazeModel =
			blazeModelMap.getOrDefault(te.getHeatLevel(), AllBlockPartials.BLAZE_HEATER_BLAZE_ONE);

		float angle;
		if (INSTANCE.player == null) {
			angle = 0;
		} else {
			Vector3f difference = new Vector3f(INSTANCE.player.getPositionVector()
				.subtract(te.getPos()
					.getX() + 0.5, 0,
					te.getPos()
						.getZ() + 0.5)
				.mul(1, 0, 1));
			difference.normalize();
			angle = (float) ((difference.getX() < 0 ? 1 : -1) * Math.acos(Direction.NORTH.getUnitVector()
				.dot(difference)));
		}
		SuperByteBuffer blazeBuffer = blazeModel.renderOn(te.getBlockState());
		blazeBuffer.rotateCentered(Direction.UP, angle);
		blazeBuffer.renderInto(ms, buffer.getBuffer(RenderType.getSolid()));
	}
}
