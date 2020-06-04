package com.simibubi.create.content.curiosities.zapper;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.block.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.block.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.PartialItemModelRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.block.FourWayBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.MathHelper;

public abstract class ZapperItemRenderer<M extends CustomRenderedItemModel> extends CustomRenderedItemModelRenderer<M> {

	@Override
	protected void render(ItemStack stack, M model, PartialItemModelRenderer renderer, MatrixStack ms,
		IRenderTypeBuffer buffer, int light, int overlay) {
		// Block indicator
		if (model.getCurrentPerspective() == TransformType.GUI && stack.hasTag() && stack.getTag()
			.contains("BlockUsed"))
			renderBlockUsed(stack, ms, buffer, light, overlay);
	}

	private void renderBlockUsed(ItemStack stack, MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {
		BlockState state = NBTUtil.readBlockState(stack.getTag()
			.getCompound("BlockUsed"));

		ms.push();
		ms.translate(-0.3F, -0.45F, -0.0F);
		ms.scale(0.25F, 0.25F, 0.25F);
		IBakedModel modelForState = Minecraft.getInstance()
			.getBlockRendererDispatcher()
			.getModelForState(state);

		if (state.getBlock() instanceof FourWayBlock)
			modelForState = Minecraft.getInstance()
				.getItemRenderer()
				.getItemModelWithOverrides(new ItemStack(state.getBlock()), Minecraft.getInstance().world, null);

		Minecraft.getInstance()
			.getItemRenderer()
			.renderItem(new ItemStack(state.getBlock()), TransformType.NONE, false, ms, buffer, light, overlay,
				modelForState);
		ms.pop();
	}

	protected float getAnimationProgress(float pt, boolean leftHanded, boolean mainHand) {
		float last = mainHand ^ leftHanded ? ZapperRenderHandler.lastRightHandAnimation
			: ZapperRenderHandler.lastLeftHandAnimation;
		float current =
			mainHand ^ leftHanded ? ZapperRenderHandler.rightHandAnimation : ZapperRenderHandler.leftHandAnimation;
		float animation = MathHelper.clamp(MathHelper.lerp(pt, last, current) * 5, 0, 1);
		return animation;
	}

}
