package com.simibubi.create.content.contraptions.processing;

import java.util.HashMap;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.SuperByteBuffer;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;

public class HeaterRenderer extends SafeTileEntityRenderer<HeaterTileEntity> {
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
		
		SuperByteBuffer blazeBuffer = blazeModel.renderOn(te.getBlockState());
		blazeBuffer.rotateCentered(Direction.UP, (float) Math.toRadians(-te.rot + (te.speed * partialTicks)));
		blazeBuffer.light(0xF000F0).renderInto(ms, buffer.getBuffer(RenderType.getSolid()));
	}
}
