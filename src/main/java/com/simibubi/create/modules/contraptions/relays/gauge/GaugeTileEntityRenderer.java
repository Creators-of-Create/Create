package com.simibubi.create.modules.contraptions.relays.gauge;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.modules.contraptions.base.IRotate;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.MathHelper;

public class GaugeTileEntityRenderer extends KineticTileEntityRenderer {

	protected GaugeBlock.Type type;

	public GaugeTileEntityRenderer(GaugeBlock.Type type) {
		this.type = type;
	}

	@Override
	public void renderTileEntityFast(KineticTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		BlockState gaugeState = te.getBlockState();
		if (!(gaugeState.getBlock() instanceof GaugeBlock))
			return;

		super.renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, buffer);
		GaugeTileEntity gaugeTE = (GaugeTileEntity) te;
		int lightCoords = gaugeState.getPackedLightmapCoords(getWorld(), te.getPos());

		BlockState head = AllBlocks.GAUGE_HEAD.get().getDefaultState().with(GaugeBlock.Head.TYPE, type);
		BlockState dial = AllBlocks.GAUGE_DIAL.get().getDefaultState();

		SuperByteBuffer headBuffer = CreateClient.bufferCache.renderGenericBlockModel(head);
		SuperByteBuffer dialBuffer = CreateClient.bufferCache.renderGenericBlockModel(dial);

		for (Direction facing : Direction.values()) {
			if (!((GaugeBlock) gaugeState.getBlock()).shouldRenderHeadOnFace(getWorld(), te.getPos(), gaugeState,
					facing))
				continue;

			float dialPivot = -5.75f / 16;
			float progress = MathHelper.lerp(partialTicks, gaugeTE.prevDialState, gaugeTE.dialState);
			dialBuffer.translate(0, dialPivot, dialPivot).rotate(Axis.X, (float) (Math.PI / 2 * -progress)).translate(0,
					-dialPivot, -dialPivot);

			rotateBufferTowards(dialBuffer, facing).light(lightCoords).translate(x, y, z).renderInto(buffer);
			rotateBufferTowards(headBuffer, facing).light(lightCoords).translate(x, y, z).renderInto(buffer);
		}

	}

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return AllBlocks.SHAFT.block.getDefaultState().with(BlockStateProperties.AXIS,
				((IRotate) te.getBlockState().getBlock()).getRotationAxis(te.getBlockState()));
	}

	protected SuperByteBuffer rotateBufferTowards(SuperByteBuffer buffer, Direction target) {
		return buffer.rotateCentered(Axis.Y, (float) ((-target.getHorizontalAngle() - 90) / 180 * Math.PI));
	}

}
