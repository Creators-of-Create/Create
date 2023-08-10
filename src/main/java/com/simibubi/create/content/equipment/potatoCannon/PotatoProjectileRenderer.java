package com.simibubi.create.content.equipment.potatoCannon;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class PotatoProjectileRenderer extends EntityRenderer<PotatoProjectileEntity> {

	public PotatoProjectileRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(PotatoProjectileEntity entity, float yaw, float pt, PoseStack ms, MultiBufferSource buffer,
		int light) {
		ItemStack item = entity.getItem();
		if (item.isEmpty())
			return;
		ms.pushPose();
		ms.translate(0, entity.getBoundingBox()
			.getYsize() / 2 - 1 / 8f, 0);
		entity.getRenderMode()
			.transform(ms, entity, pt);

		Minecraft.getInstance()
			.getItemRenderer()
			.renderStatic(item, TransformType.GROUND, light, OverlayTexture.NO_OVERLAY, ms, buffer, 0);
		ms.popPose();
	}

	@Override
	public ResourceLocation getTextureLocation(PotatoProjectileEntity entity) {
		return null;
	}

}
