package com.simibubi.create.content.curiosities.toolbox;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.renderer.SmartTileEntityRenderer;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;

public class ToolboxRenderer extends SmartTileEntityRenderer<ToolboxTileEntity> {

	public ToolboxRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(ToolboxTileEntity tileEntityIn, float partialTicks, MatrixStack ms,
		IRenderTypeBuffer buffer, int light, int overlay) {

		BlockState blockState = tileEntityIn.getBlockState();
		Direction facing = blockState.getValue(ToolboxBlock.FACING)
			.getOpposite();
		SuperByteBuffer lid =
			PartialBufferer.get(AllBlockPartials.TOOLBOX_LIDS.get(tileEntityIn.getColor()), blockState);
		SuperByteBuffer drawer = PartialBufferer.get(AllBlockPartials.TOOLBOX_DRAWER, blockState);

		float lidAngle = tileEntityIn.lid.getValue(partialTicks);
		float drawerOffset = tileEntityIn.drawers.getValue(partialTicks);

		IVertexBuilder layer = buffer.getBuffer(RenderType.solid());
		lid.matrixStacker()
			.centre()
			.rotateY(-facing.toYRot())
			.unCentre()
			.translate(0, 6 / 16f, 12 / 16f)
			.rotateX(135 * lidAngle)
			.translate(0, -6 / 16f, -12 / 16f);
		lid.light(light)
			.renderInto(ms, layer);

		for (int offset : Iterate.zeroAndOne) {
			drawer.matrixStacker()
				.centre()
				.rotateY(-facing.toYRot())
				.unCentre();
			drawer.translate(0, offset * 1 / 8f, -drawerOffset * .175f * (2 - offset))
				.light(light)
				.renderInto(ms, layer);
		}

	}

}
