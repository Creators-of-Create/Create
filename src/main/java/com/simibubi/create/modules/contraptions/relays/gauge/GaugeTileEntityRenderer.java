package com.simibubi.create.modules.contraptions.relays.gauge;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocksNew;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.modules.contraptions.base.IRotate;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.modules.contraptions.relays.gauge.GaugeBlock.Type;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.MathHelper;

public class GaugeTileEntityRenderer extends KineticTileEntityRenderer {

	protected GaugeBlock.Type type;

	public GaugeTileEntityRenderer(TileEntityRendererDispatcher dispatcher, GaugeBlock.Type type) {
		super(dispatcher);
		this.type = type;
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
			int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
		BlockState gaugeState = te.getBlockState();
		GaugeTileEntity gaugeTE = (GaugeTileEntity) te;
		int lightCoords = WorldRenderer.getLightmapCoordinates(te.getWorld(), gaugeState, te.getPos());

		SuperByteBuffer headBuffer = (type == Type.SPEED ? AllBlockPartials.GAUGE_HEAD_SPEED
				: AllBlockPartials.GAUGE_HEAD_STRESS).renderOn(gaugeState);
		SuperByteBuffer dialBuffer = AllBlockPartials.GAUGE_DIAL.renderOn(gaugeState);

		for (Direction facing : Direction.values()) {
			if (!((GaugeBlock) gaugeState.getBlock()).shouldRenderHeadOnFace(te.getWorld(), te.getPos(), gaugeState,
					facing))
				continue;

			float dialPivot = -5.75f / 16;
			float progress = MathHelper.lerp(partialTicks, gaugeTE.prevDialState, gaugeTE.dialState);
			dialBuffer.translate(0, dialPivot, dialPivot).rotate(Axis.X, (float) (Math.PI / 2 * -progress)).translate(0,
					-dialPivot, -dialPivot);

			IVertexBuilder vb = buffer.getBuffer(RenderType.getSolid());
			rotateBufferTowards(dialBuffer, facing).light(lightCoords).renderInto(ms, vb);
			rotateBufferTowards(headBuffer, facing).light(lightCoords).renderInto(ms, vb);
		}

	}

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return AllBlocksNew.SHAFT.getDefault().with(BlockStateProperties.AXIS,
				((IRotate) te.getBlockState().getBlock()).getRotationAxis(te.getBlockState()));
	}

	protected SuperByteBuffer rotateBufferTowards(SuperByteBuffer buffer, Direction target) {
		return buffer.rotateCentered(Axis.Y, (float) ((-target.getHorizontalAngle() - 90) / 180 * Math.PI));
	}

}
