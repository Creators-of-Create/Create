package com.simibubi.create.content.contraptions.relays.advanced;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.renderer.SmartTileEntityRenderer;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class SpeedControllerRenderer extends SmartTileEntityRenderer<SpeedControllerTileEntity> {

	public SpeedControllerRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(SpeedControllerTileEntity tileEntityIn, float partialTicks, PoseStack ms,
		MultiBufferSource buffer, int light, int overlay) {
		super.renderSafe(tileEntityIn, partialTicks, ms, buffer, light, overlay);

		VertexConsumer builder = buffer.getBuffer(RenderType.solid());
		if (!Backend.getInstance().canUseInstancing(tileEntityIn.getLevel())) {
			KineticTileEntityRenderer.renderRotatingBuffer(tileEntityIn, getRotatedModel(tileEntityIn), ms, builder, light);
		}

		if (!tileEntityIn.hasBracket)
			return;

		BlockPos pos = tileEntityIn.getBlockPos();
		Level world = tileEntityIn.getLevel();
		BlockState blockState = tileEntityIn.getBlockState();
		boolean alongX = blockState.getValue(SpeedControllerBlock.HORIZONTAL_AXIS) == Axis.X;

		SuperByteBuffer bracket = CachedBufferer.partial(AllBlockPartials.SPEED_CONTROLLER_BRACKET, blockState);
		bracket.translate(0, 1, 0);
		bracket.rotateCentered(Direction.UP,
				(float) (alongX ? Math.PI : Math.PI / 2));
		bracket.light(LevelRenderer.getLightColor(world, pos.above()));
		bracket.renderInto(ms, builder);
	}

	private SuperByteBuffer getRotatedModel(SpeedControllerTileEntity te) {
		return CachedBufferer.block(KineticTileEntityRenderer.KINETIC_TILE,
				KineticTileEntityRenderer.shaft(KineticTileEntityRenderer.getRotationAxisOf(te)));
	}

}
