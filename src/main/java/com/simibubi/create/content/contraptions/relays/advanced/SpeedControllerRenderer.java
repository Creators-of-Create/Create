package com.simibubi.create.content.contraptions.relays.advanced;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.tileEntity.renderer.SmartTileEntityRenderer;
import com.simibubi.create.foundation.utility.SuperByteBuffer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpeedControllerRenderer extends SmartTileEntityRenderer<SpeedControllerTileEntity> {

	public SpeedControllerRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(SpeedControllerTileEntity tileEntityIn, float partialTicks, MatrixStack ms,
		IRenderTypeBuffer buffer, int light, int overlay) {
		super.renderSafe(tileEntityIn, partialTicks, ms, buffer, light, overlay);

		IVertexBuilder builder = buffer.getBuffer(RenderType.getSolid());
		KineticTileEntityRenderer.renderRotatingBuffer(tileEntityIn, getRotatedModel(tileEntityIn), ms, builder, light);

		if (!tileEntityIn.hasBracket)
			return;

		BlockPos pos = tileEntityIn.getPos();
		World world = tileEntityIn.getWorld();
		BlockState blockState = tileEntityIn.getBlockState();

		SuperByteBuffer bracket = AllBlockPartials.SPEED_CONTROLLER_BRACKET.renderOn(blockState);
		bracket.translate(0, 1, 0);
		bracket.rotateCentered(Direction.UP,
			(float) (blockState.get(SpeedControllerBlock.HORIZONTAL_AXIS) == Axis.X ? Math.PI : 0));
		bracket.light(WorldRenderer.getLightmapCoordinates(world, pos.up()));
		bracket.renderInto(ms, builder);
	}

	private SuperByteBuffer getRotatedModel(SpeedControllerTileEntity te) {
		return CreateClient.bufferCache.renderBlockIn(KineticTileEntityRenderer.KINETIC_TILE,
			KineticTileEntityRenderer.shaft(KineticTileEntityRenderer.getRotationAxisOf(te)));
	}

}
