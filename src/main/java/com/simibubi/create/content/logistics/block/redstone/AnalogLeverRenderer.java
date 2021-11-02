package com.simibubi.create.content.logistics.block.redstone;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Color;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.Direction;

public class AnalogLeverRenderer extends SafeTileEntityRenderer<AnalogLeverTileEntity> {

	public AnalogLeverRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(AnalogLeverTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {

		if (Backend.getInstance().canUseInstancing(te.getLevel())) return;

		BlockState leverState = te.getBlockState();
		float state = te.clientState.get(partialTicks);

		IVertexBuilder vb = buffer.getBuffer(RenderType.solid());

		// Handle
		SuperByteBuffer handle = PartialBufferer.get(AllBlockPartials.ANALOG_LEVER_HANDLE, leverState);
		float angle = (float) ((state / 15) * 90 / 180 * Math.PI);
		transform(handle, leverState).translate(1 / 2f, 1 / 16f, 1 / 2f)
				.rotate(Direction.EAST, angle)
				.translate(-1 / 2f, -1 / 16f, -1 / 2f);
		handle.light(light)
				.renderInto(ms, vb);

		// Indicator
		int color = Color.mixColors(0x2C0300, 0xCD0000, state / 15f);
		SuperByteBuffer indicator = transform(PartialBufferer.get(AllBlockPartials.ANALOG_LEVER_INDICATOR, leverState), leverState);
		indicator.light(light)
				.color(color)
				.renderInto(ms, vb);
	}

	private SuperByteBuffer transform(SuperByteBuffer buffer, BlockState leverState) {
		AttachFace face = leverState.getValue(AnalogLeverBlock.FACE);
		float rX = face == AttachFace.FLOOR ? 0 : face == AttachFace.WALL ? 90 : 180;
		float rY = AngleHelper.horizontalAngle(leverState.getValue(AnalogLeverBlock.FACING));
		buffer.rotateCentered(Direction.UP, (float) (rY / 180 * Math.PI));
		buffer.rotateCentered(Direction.EAST, (float) (rX / 180 * Math.PI));
		return buffer;
	}

}
