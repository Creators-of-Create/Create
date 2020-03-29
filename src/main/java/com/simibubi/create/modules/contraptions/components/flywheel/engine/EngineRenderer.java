package com.simibubi.create.modules.contraptions.components.flywheel.engine;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.block.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;

public class EngineRenderer<T extends EngineTileEntity> extends SafeTileEntityRenderer<T> {

	public EngineRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderFast(T te, double x, double y, double z, float partialTicks, int destroyStage,
			BufferBuilder buffer) {
		Block block = te.getBlockState().getBlock();
		if (block instanceof EngineBlock) {
			EngineBlock engineBlock = (EngineBlock) block;
			AllBlockPartials frame = engineBlock.getFrameModel();
			if (frame != null) {
				Direction facing = te.getBlockState().get(EngineBlock.HORIZONTAL_FACING);
				float angle = AngleHelper.rad(AngleHelper.horizontalAngle(facing));
				frame.renderOn(te.getBlockState()).translate(0, 0, -1).rotateCentered(Axis.Y, angle).translate(x, y, z)
						.light(te.getBlockState().getPackedLightmapCoords(getWorld(), te.getPos())).renderInto(buffer);
			}
		}
	}

}
