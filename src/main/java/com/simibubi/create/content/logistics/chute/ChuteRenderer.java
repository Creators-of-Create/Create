package com.simibubi.create.content.logistics.chute;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.chute.ChuteBlock.Shape;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;

public class ChuteRenderer extends SafeBlockEntityRenderer<ChuteBlockEntity> {

	public ChuteRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(ChuteBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light,
		int overlay) {
		if (be.item.isEmpty())
			return;
		BlockState blockState = be.getBlockState();
		if (blockState.getValue(ChuteBlock.FACING) != Direction.DOWN)
			return;
		if (blockState.getValue(ChuteBlock.SHAPE) != Shape.WINDOW
			&& (be.bottomPullDistance == 0 || be.itemPosition.getValue(partialTicks) > .5f))
			return;

		renderItem(be, partialTicks, ms, buffer, light, overlay);
	}

	public static void renderItem(ChuteBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		ItemRenderer itemRenderer = Minecraft.getInstance()
			.getItemRenderer();
		TransformStack msr = TransformStack.cast(ms);
		ms.pushPose();
		msr.centre();
		float itemScale = .5f;
		float itemPosition = be.itemPosition.getValue(partialTicks);
		ms.translate(0, -.5 + itemPosition, 0);
		ms.scale(itemScale, itemScale, itemScale);
		msr.rotateX(itemPosition * 180);
		msr.rotateY(itemPosition * 180);
		itemRenderer.renderStatic(be.item, ItemDisplayContext.FIXED, light, overlay, ms, buffer, be.getLevel(), 0);
		ms.popPose();
	}

}
