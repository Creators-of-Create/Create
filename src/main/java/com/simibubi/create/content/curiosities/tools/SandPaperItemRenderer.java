package com.simibubi.create.content.curiosities.tools;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class SandPaperItemRenderer extends ItemStackTileEntityRenderer {

	@Override
	public void renderByItem(ItemStack stack, TransformType transformType, MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		ClientPlayerEntity player = Minecraft.getInstance().player;
		SandPaperModel mainModel = (SandPaperModel) itemRenderer.getModel(stack, Minecraft.getInstance().level, null);
		float partialTicks = AnimationTickHolder.getPartialTicks();

		boolean leftHand = transformType == TransformType.FIRST_PERSON_LEFT_HAND;
		boolean firstPerson = leftHand || transformType == TransformType.FIRST_PERSON_RIGHT_HAND;

		ms.pushPose();
		ms.translate(.5f, .5f, .5f);

		CompoundNBT tag = stack.getOrCreateTag();
		boolean jeiMode = tag.contains("JEI");

		if (tag.contains("Polishing")) {
			ms.pushPose();

			if (transformType == TransformType.GUI) {
				ms.translate(0.0F, .2f, 1.0F);
				ms.scale(.75f, .75f, .75f);
			} else {
				int modifier = leftHand ? -1 : 1;
				ms.mulPose(Vector3f.YP.rotationDegrees(modifier * 40));
			}

			// Reverse bobbing
			float time = (float) (!jeiMode ? player.getUseItemRemainingTicks()
					: (-AnimationTickHolder.getTicks()) % stack.getUseDuration()) - partialTicks + 1.0F;
			if (time / (float) stack.getUseDuration() < 0.8F) {
				float bobbing = -MathHelper.abs(MathHelper.cos(time / 4.0F * (float) Math.PI) * 0.1F);

				if (transformType == TransformType.GUI)
					ms.translate(bobbing, bobbing, 0.0F);
				else
					ms.translate(0.0f, bobbing, 0.0F);
			}

			ItemStack toPolish = ItemStack.of(tag.getCompound("Polishing"));
			itemRenderer.renderStatic(toPolish, TransformType.NONE, light, overlay, ms, buffer);

			ms.popPose();
		}

		if (firstPerson) {
			int itemInUseCount = player.getUseItemRemainingTicks();
			if (itemInUseCount > 0) {
				int modifier = leftHand ? -1 : 1;
				ms.translate(modifier * .5f, 0, -.25f);
				ms.mulPose(Vector3f.ZP.rotationDegrees(modifier * 40));
				ms.mulPose(Vector3f.XP.rotationDegrees(modifier * 10));
				ms.mulPose(Vector3f.YP.rotationDegrees(modifier * 90));
			}
		}

		itemRenderer.render(stack, TransformType.NONE, false, ms, buffer, light, overlay, mainModel.getOriginalModel());

		ms.popPose();
	}

	public static class SandPaperModel extends CustomRenderedItemModel {

		public SandPaperModel(IBakedModel template) {
			super(template, "create","");
		}

		@Override
		public ItemStackTileEntityRenderer createRenderer() {
			return new SandPaperItemRenderer();
		}

	}

}
