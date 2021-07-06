package com.simibubi.create.content.logistics.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

public class LecternControllerRenderer extends SafeTileEntityRenderer<LecternControllerTileEntity> {

	public LecternControllerRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(LecternControllerTileEntity te, float partialTicks, MatrixStack ms,
  		IRenderTypeBuffer buffer, int light, int overlay) {

		ItemStack stack = AllItems.LINKED_CONTROLLER.asStack();
		TransformType transformType = TransformType.NONE;
		LinkedControllerModel mainModel = ((LinkedControllerModel) Minecraft.getInstance()
			.getItemRenderer()
			.getItemModelWithOverrides(stack, null, null));
		PartialItemModelRenderer renderer = PartialItemModelRenderer.of(stack, transformType, ms, buffer, overlay);
		boolean active = te.hasUser();
		boolean usedByMe = te.isUsedBy(Minecraft.getInstance().player);

		Direction facing = te.getBlockState().get(LecternControllerBlock.FACING);
		MatrixStacker msr = MatrixStacker.of(ms);

		ms.push();
		msr.translate(0.5, 1.45, 0.5);
		msr.rotateY(AngleHelper.horizontalAngle(facing) - 90);
		msr.translate(0.28, 0, 0);
		msr.rotateZ(-22.0);
		LinkedControllerItemRenderer.renderLinkedController(stack, mainModel, renderer, transformType, ms, light, active, usedByMe);
		ms.pop();
	}

}
