package com.simibubi.create.content.equipment.bell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedPartialBuffers;

import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.utility.math.AngleHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;

public class BellRenderer<BE extends AbstractBellBlockEntity> extends SafeBlockEntityRenderer<BE> {

	public BellRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	protected void renderSafe(BE be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		BlockState state = be.getBlockState();
		Direction facing = state.getValue(BellBlock.FACING);
		BellAttachType attachment = state.getValue(BellBlock.ATTACHMENT);

		SuperByteBuffer bell = CachedPartialBuffers.partial(be.getBellModel(), state);

		if (be.isRinging)
			bell.rotateCentered(be.ringDirection.getCounterClockWise(), getSwingAngle(be.ringingTicks + partialTicks));

		float rY = AngleHelper.horizontalAngle(facing);
		if (attachment == BellAttachType.SINGLE_WALL || attachment == BellAttachType.DOUBLE_WALL)
			rY += 90;
		bell.rotateCentered(Direction.UP, AngleHelper.rad(rY));

		bell.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.cutout()));
	}

	public static float getSwingAngle(float time) {
		float t = time / 1.5f;
		return 1.2f * Mth.sin(t / (float) Math.PI) / (2.5f + t / 3.0f);
	}

}
