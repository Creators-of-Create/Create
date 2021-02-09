package com.simibubi.create.content.curiosities.tools;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.block.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;

public class SandPaperItemRenderer extends ItemStackTileEntityRenderer {

	@Override
	public void render(ItemStack stack, MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		ClientPlayerEntity player = Minecraft.getInstance().player;
		SandPaperModel mainModel = (SandPaperModel) itemRenderer.getItemModelWithOverrides(stack, Minecraft.getInstance().world, null);
		TransformType perspective = mainModel.getCurrentPerspective();
		float partialTicks = AnimationTickHolder.getPartialTicks();

		boolean leftHand = perspective == TransformType.FIRST_PERSON_LEFT_HAND;
		boolean firstPerson = leftHand || perspective == TransformType.FIRST_PERSON_RIGHT_HAND;

		ms.push();
		ms.translate(.5f, .5f, .5f);

		CompoundNBT tag = stack.getOrCreateTag();
		boolean jeiMode = tag.contains("JEI");

		if (tag.contains("Polishing")) {
			ms.push();

			if (perspective == TransformType.GUI) {
				ms.translate(0.0F, .2f, 1.0F);
				ms.scale(.75f, .75f, .75f);
			} else {
				int modifier = leftHand ? -1 : 1;
				ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(modifier * 40));
			}

			// Reverse bobbing
			float time = (float) (!jeiMode ? player.getItemInUseCount()
					: (-AnimationTickHolder.getTicks()) % stack.getUseDuration()) - partialTicks + 1.0F;
			if (time / (float) stack.getUseDuration() < 0.8F) {
				float bobbing = -MathHelper.abs(MathHelper.cos(time / 4.0F * (float) Math.PI) * 0.1F);

				if (perspective == TransformType.GUI)
					ms.translate(bobbing, bobbing, 0.0F);
				else
					ms.translate(0.0f, bobbing, 0.0F);
			}

			ItemStack toPolish = ItemStack.read(tag.getCompound("Polishing"));
			itemRenderer.renderItem(toPolish, TransformType.NONE, light, overlay, ms, buffer);

			ms.pop();
		}

		if (firstPerson) {
			int itemInUseCount = player.getItemInUseCount();
			if (itemInUseCount > 0) {
				int modifier = leftHand ? -1 : 1;
				RenderSystem.translatef(modifier * .5f, 0, -.25f);
				RenderSystem.rotatef(modifier * 40, 0, 0, 1);
				RenderSystem.rotatef(modifier * 10, 1, 0, 0);
				RenderSystem.rotatef(modifier * 90, 0, 1, 0);
			}
		}

		itemRenderer.renderItem(stack, TransformType.NONE, false, ms, buffer, light, overlay, mainModel.getBakedModel());

		ms.pop();
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
