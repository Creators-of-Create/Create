package com.simibubi.create.content.logistics.item;

import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;

public class LecternControllerRenderer extends SafeTileEntityRenderer<LecternControllerTileEntity> {

	public LecternControllerRenderer(BlockEntityRenderDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(LecternControllerTileEntity te, float partialTicks, PoseStack ms,
  		MultiBufferSource buffer, int light, int overlay) {

		ItemStack stack = AllItems.LINKED_CONTROLLER.asStack();
		TransformType transformType = TransformType.NONE;
		LinkedControllerModel mainModel = (LinkedControllerModel) Minecraft.getInstance()
			.getItemRenderer()
			.getModel(stack, null, null);
		PartialItemModelRenderer renderer = PartialItemModelRenderer.of(stack, transformType, ms, buffer, overlay);
		boolean active = te.hasUser();
		boolean renderDepression = te.isUsedBy(Minecraft.getInstance().player);

		Direction facing = te.getBlockState().getValue(LecternControllerBlock.FACING);
		MatrixTransformStack msr = MatrixTransformStack.of(ms);

		ms.pushPose();
		msr.translate(0.5, 1.45, 0.5);
		msr.rotateY(AngleHelper.horizontalAngle(facing) - 90);
		msr.translate(0.28, 0, 0);
		msr.rotateZ(-22.0);
		LinkedControllerItemRenderer.renderInLectern(stack, mainModel, renderer, transformType, ms, light, active, renderDepression);
		ms.popPose();
	}

}
