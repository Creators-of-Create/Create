package com.simibubi.create.modules.curiosities.tools;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.foundation.block.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;

@SuppressWarnings("deprecation")
public class SandPaperItemRenderer extends ItemStackTileEntityRenderer {

	@Override
	public void renderByItem(ItemStack stack) {
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		ClientPlayerEntity player = Minecraft.getInstance().player;
		SandPaperModel mainModel = (SandPaperModel) itemRenderer.getModelWithOverrides(stack);
		TransformType perspective = mainModel.getCurrentPerspective();
		float partialTicks = Minecraft.getInstance().getRenderPartialTicks();

		boolean leftHand = perspective == TransformType.FIRST_PERSON_LEFT_HAND;
		boolean firstPerson = leftHand || perspective == TransformType.FIRST_PERSON_RIGHT_HAND;

		GlStateManager.pushMatrix();
		GlStateManager.translatef(.5f, .5f, .5f);

		CompoundNBT tag = stack.getOrCreateTag();
		boolean jeiMode = tag.contains("JEI");

		if (tag.contains("Polishing")) {
			GlStateManager.pushMatrix();

			if (perspective == TransformType.GUI) {
				GlStateManager.translatef(0.0F, .2f, 1.0F);
				GlStateManager.scalef(.75f, .75f, .75f);
			} else {
				int modifier = leftHand ? -1 : 1;
				GlStateManager.rotatef(modifier * 40, 0, 1, 0);
			}

			// Reverse bobbing
			float time = (float) (!jeiMode ? player.getItemInUseCount()
					: (-AnimationTickHolder.ticks) % stack.getUseDuration()) - partialTicks + 1.0F;
			if (time / (float) stack.getUseDuration() < 0.8F) {
				float bobbing = -MathHelper.abs(MathHelper.cos(time / 4.0F * (float) Math.PI) * 0.1F);

				if (perspective == TransformType.GUI)
					GlStateManager.translatef(bobbing, bobbing, 0.0F);
				else
					GlStateManager.translatef(0.0f, bobbing, 0.0F);
			}

			ItemStack toPolish = ItemStack.read(tag.getCompound("Polishing"));
			itemRenderer.renderItem(toPolish, itemRenderer.getModelWithOverrides(toPolish).getBakedModel());

			GlStateManager.popMatrix();
		}

		if (firstPerson) {
			int itemInUseCount = player.getItemInUseCount();
			if (itemInUseCount > 0) {
				int modifier = leftHand ? -1 : 1;
				GlStateManager.translatef(modifier * .5f, 0, -.25f);
				GlStateManager.rotatef(modifier * 40, 0, 0, 1);
				GlStateManager.rotatef(modifier * 10, 1, 0, 0);
				GlStateManager.rotatef(modifier * 90, 0, 1, 0);
			}
		}

		itemRenderer.renderItem(stack, mainModel.getBakedModel());

		GlStateManager.popMatrix();
	}

	public static class SandPaperModel extends CustomRenderedItemModel {

		public SandPaperModel(IBakedModel template) {
			super(template, "");
		}

		@Override
		public ItemStackTileEntityRenderer createRenderer() {
			return new SandPaperItemRenderer();
		}

	}

}
