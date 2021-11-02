package com.simibubi.create.content.curiosities.toolbox;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.renderer.SmartTileEntityRenderer;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class ToolboxRenderer extends SmartTileEntityRenderer<ToolboxTileEntity> {

	public ToolboxRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(ToolboxTileEntity tileEntityIn, float partialTicks, PoseStack ms,
		MultiBufferSource buffer, int light, int overlay) {

		BlockState blockState = tileEntityIn.getBlockState();
		Direction facing = blockState.getValue(ToolboxBlock.FACING)
			.getOpposite();
		SuperByteBuffer lid =
			PartialBufferer.get(AllBlockPartials.TOOLBOX_LIDS.get(tileEntityIn.getColor()), blockState);
		SuperByteBuffer drawer = PartialBufferer.get(AllBlockPartials.TOOLBOX_DRAWER, blockState);

		float lidAngle = tileEntityIn.lid.getValue(partialTicks);
		float drawerOffset = tileEntityIn.drawers.getValue(partialTicks);

		VertexConsumer builder = buffer.getBuffer(RenderType.cutoutMipped());
		lid.matrixStacker()
			.centre()
			.rotateY(-facing.toYRot())
			.unCentre()
			.translate(0, 6 / 16f, 12 / 16f)
			.rotateX(135 * lidAngle)
			.translate(0, -6 / 16f, -12 / 16f);
		lid.light(light)
			.renderInto(ms, builder);

		for (int offset : Iterate.zeroAndOne) {
			drawer.matrixStacker()
				.centre()
				.rotateY(-facing.toYRot())
				.unCentre();
			drawer.translate(0, offset * 1 / 8f, -drawerOffset * .175f * (2 - offset))
				.light(light)
				.renderInto(ms, builder);
		}

	}

}
