package com.simibubi.create.modules.contraptions.redstone;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.SuperByteBuffer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.Direction.Axis;
import net.minecraftforge.client.model.animation.TileEntityRendererFast;

public class AnalogLeverTileEntityRenderer extends TileEntityRendererFast<AnalogLeverTileEntity> {

	@Override
	public void renderTileEntityFast(AnalogLeverTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		BlockState leverState = te.getBlockState();
		if (!AllBlocks.ANALOG_LEVER.typeOf(leverState))
			return;

		int lightCoords = leverState.getPackedLightmapCoords(getWorld(), te.getPos());
		float state = te.clientState.get(partialTicks);

		// Handle
		SuperByteBuffer handle = render(AllBlocks.ANALOG_LEVER_HANDLE);
		float angle = (float) ((state / 15) * 90 / 180 * Math.PI);
		handle.translate(-1 / 2f, -1 / 16f, -1 / 2f).rotate(Axis.X, angle).translate(1 / 2f, 1 / 16f, 1 / 2f);
		transform(handle, leverState).light(lightCoords).translate(x, y, z).renderInto(buffer);

		// Indicator
		int color = ColorHelper.mixColors(0x2C0300, 0xCD0000, state / 15f);
		SuperByteBuffer indicator = transform(render(AllBlocks.ANALOG_LEVER_INDICATOR), leverState);
		indicator.light(lightCoords).translate(x, y, z).color(color).renderInto(buffer);
	}

	private SuperByteBuffer render(AllBlocks model) {
		return CreateClient.bufferCache.renderGenericBlockModel(model.getDefault());
	}

	private SuperByteBuffer transform(SuperByteBuffer buffer, BlockState leverState) {
		AttachFace face = leverState.get(AnalogLeverBlock.FACE);
		float rX = face == AttachFace.FLOOR ? 0 : face == AttachFace.WALL ? 90 : 180;
		float rY = AngleHelper.horizontalAngle(leverState.get(AnalogLeverBlock.HORIZONTAL_FACING));
		buffer.rotateCentered(Axis.X, (float) (rX / 180 * Math.PI));
		buffer.rotateCentered(Axis.Y, (float) (rY / 180 * Math.PI));
		return buffer;
	}

}
