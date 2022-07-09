package com.simibubi.create.content.curiosities.deco;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;

public class SlidingDoorRenderer extends SafeTileEntityRenderer<SlidingDoorTileEntity> {

	public SlidingDoorRenderer(Context context) {}

	@Override
	protected void renderSafe(SlidingDoorTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		BlockState blockState = te.getBlockState();
		if (!te.shouldRenderSpecial(blockState))
			return;

		Direction facing = blockState.getValue(DoorBlock.FACING);
		Direction movementDirection = facing.getClockWise();

		if (blockState.getValue(DoorBlock.HINGE) == DoorHingeSide.LEFT)
			movementDirection = movementDirection.getOpposite();

		float value = te.animation.getValue(partialTicks);
		float value2 = Mth.clamp(value * 10, 0, 1);

		Vec3 offset = Vec3.atLowerCornerOf(movementDirection.getNormal())
			.scale(value * value * 13 / 16f)
			.add(Vec3.atLowerCornerOf(facing.getNormal())
				.scale(value2 * 1 / 32f));

		VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());
		for (DoubleBlockHalf half : DoubleBlockHalf.values()) {
			CachedBufferer.block(blockState.setValue(DoorBlock.OPEN, false)
				.setValue(DoorBlock.HALF, half))
				.translate(0, half == DoubleBlockHalf.UPPER ? 1 - 1 / 512f : 0, 0)
				.translate(offset)
				.light(light)
				.renderInto(ms, vb);
		}

	}

}
