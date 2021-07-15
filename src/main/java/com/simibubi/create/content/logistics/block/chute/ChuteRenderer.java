package com.simibubi.create.content.logistics.block.chute;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.logistics.block.chute.ChuteBlock.Shape;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.MatrixStacker;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;

public class ChuteRenderer extends SafeTileEntityRenderer<ChuteTileEntity> {

	public ChuteRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(ChuteTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		if (te.item.isEmpty())
			return;
		BlockState blockState = te.getBlockState();
		if (blockState.getValue(ChuteBlock.FACING) != Direction.DOWN)
			return;
		if (blockState.getValue(ChuteBlock.SHAPE) != Shape.WINDOW
			&& (te.bottomPullDistance == 0 || te.itemPosition.get(partialTicks) > .5f))
			return;

		renderItem(te, partialTicks, ms, buffer, light, overlay);
	}

	public static void renderItem(ChuteTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		ItemRenderer itemRenderer = Minecraft.getInstance()
			.getItemRenderer();
		MatrixStacker msr = MatrixStacker.of(ms);
		ms.pushPose();
		msr.centre();
		float itemScale = .5f;
		float itemPosition = te.itemPosition.get(partialTicks);
		ms.translate(0, -.5 + itemPosition, 0);
		ms.scale(itemScale, itemScale, itemScale);
		msr.rotateX(itemPosition * 180);
		msr.rotateY(itemPosition * 180);
		itemRenderer.renderStatic(te.item, TransformType.FIXED, light, overlay, ms, buffer);
		ms.popPose();
	}

}
