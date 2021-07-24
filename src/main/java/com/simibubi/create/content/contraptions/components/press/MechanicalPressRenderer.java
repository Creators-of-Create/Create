package com.simibubi.create.content.contraptions.components.press;

import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.BlockPos;

public class MechanicalPressRenderer extends KineticTileEntityRenderer {

	public MechanicalPressRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	public boolean shouldRenderOffScreen(KineticTileEntity te) {
		return true;
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
			int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);

		if (Backend.getInstance().canUseInstancing(te.getLevel())) return;

		BlockPos pos = te.getBlockPos();
		BlockState blockState = te.getBlockState();
		int packedLightmapCoords = WorldRenderer.getLightColor(te.getLevel(), blockState, pos);
		float renderedHeadOffset = ((MechanicalPressTileEntity) te).getRenderedHeadOffset(partialTicks);

		SuperByteBuffer headRender = PartialBufferer.getFacing(AllBlockPartials.MECHANICAL_PRESS_HEAD, blockState, blockState.getValue(HORIZONTAL_FACING));
		headRender.translate(0, -renderedHeadOffset, 0)
				.light(packedLightmapCoords)
				.renderInto(ms, buffer.getBuffer(RenderType.solid()));
	}

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return shaft(getRotationAxisOf(te));
	}

}
