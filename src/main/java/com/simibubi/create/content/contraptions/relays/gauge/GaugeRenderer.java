package com.simibubi.create.content.contraptions.relays.gauge;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.relays.gauge.GaugeBlock.Type;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.backend.FastRenderDispatcher;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;

public class GaugeRenderer extends KineticTileEntityRenderer {

	protected GaugeBlock.Type type;

	public static GaugeRenderer speed(TileEntityRendererDispatcher dispatcher) {
		return new GaugeRenderer(dispatcher, Type.SPEED);
	}

	public static GaugeRenderer stress(TileEntityRendererDispatcher dispatcher) {
		return new GaugeRenderer(dispatcher, Type.STRESS);
	}

	protected GaugeRenderer(TileEntityRendererDispatcher dispatcher, GaugeBlock.Type type) {
		super(dispatcher);
		this.type = type;
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		if (FastRenderDispatcher.available(te.getWorld())) return;

		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
		BlockState gaugeState = te.getBlockState();
		GaugeTileEntity gaugeTE = (GaugeTileEntity) te;
		int lightCoords = WorldRenderer.getLightmapCoordinates(te.getWorld(), gaugeState, te.getPos());

		AllBlockPartials allBlockPartials = (type == Type.SPEED ? AllBlockPartials.GAUGE_HEAD_SPEED : AllBlockPartials.GAUGE_HEAD_STRESS);
		SuperByteBuffer headBuffer =
				PartialBufferer.get(allBlockPartials, gaugeState);
		SuperByteBuffer dialBuffer = PartialBufferer.get(AllBlockPartials.GAUGE_DIAL, gaugeState);

		float dialPivot = 5.75f / 16;
		float progress = MathHelper.lerp(partialTicks, gaugeTE.prevDialState, gaugeTE.dialState);

		for (Direction facing : Iterate.directions) {
			if (!((GaugeBlock) gaugeState.getBlock()).shouldRenderHeadOnFace(te.getWorld(), te.getPos(), gaugeState,
					facing))
				continue;

			IVertexBuilder vb = buffer.getBuffer(RenderType.getSolid());
			rotateBufferTowards(dialBuffer, facing).translate(0, dialPivot, dialPivot)
				.rotate(Direction.EAST, (float) (Math.PI / 2 * -progress))
				.translate(0, -dialPivot, -dialPivot)
				.light(lightCoords)
				.renderInto(ms, vb);
			rotateBufferTowards(headBuffer, facing).light(lightCoords)
				.renderInto(ms, vb);
		}

	}

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return shaft(getRotationAxisOf(te));
	}

	protected SuperByteBuffer rotateBufferTowards(SuperByteBuffer buffer, Direction target) {
		return buffer.rotateCentered(Direction.UP, (float) ((-target.getHorizontalAngle() - 90) / 180 * Math.PI));
	}

}
