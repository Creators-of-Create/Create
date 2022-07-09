package com.simibubi.create.content.curiosities.deco;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

public class PlacardRenderer extends SafeTileEntityRenderer<PlacardTileEntity> {

	public PlacardRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(PlacardTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		ItemStack heldItem = te.getHeldItem();
		if (heldItem.isEmpty())
			return;

		BlockState blockState = te.getBlockState();
		Direction facing = blockState.getValue(PlacardBlock.FACING);
		AttachFace face = blockState.getValue(PlacardBlock.FACE);

		ItemRenderer itemRenderer = Minecraft.getInstance()
			.getItemRenderer();
		boolean blockItem = itemRenderer.getModel(heldItem, null, null, 0)
			.isGui3d();

		ms.pushPose();
		TransformStack.cast(ms)
			.centre()
			.rotate(Direction.UP,
				(face == AttachFace.CEILING ? Mth.PI : 0) + AngleHelper.rad(180 + AngleHelper.horizontalAngle(facing)))
			.rotate(Direction.EAST,
				face == AttachFace.CEILING ? -Mth.PI / 2 : face == AttachFace.FLOOR ? Mth.PI / 2 : 0)
			.translate(0, 0, 4.5 / 16f)
			.scale(blockItem ? .5f : .375f);

		itemRenderer.renderStatic(heldItem, TransformType.FIXED, light, overlay, ms, buffer, 0);
		ms.popPose();
	}

}
