package com.simibubi.create.content.curiosities.weapons;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class PotatoProjectileRenderer extends EntityRenderer<PotatoProjectileEntity> {

	public PotatoProjectileRenderer(EntityRendererManager p_i46179_1_) {
		super(p_i46179_1_);
	}

	@Override
	public void render(PotatoProjectileEntity entity, float yaw, float pt, MatrixStack ms, IRenderTypeBuffer buffer,
		int light) {
		ItemStack item = entity.getItem();
		if (item.isEmpty())
			return;
		ms.push();
		ms.translate(0, entity.getBoundingBox()
			.getYSize() / 2 - 1 / 8f, 0);
		entity.getProjectileType()
			.getRenderMode()
			.transform(ms, entity, pt);

		Minecraft.getInstance()
			.getItemRenderer()
			.renderItem(item, TransformType.GROUND, light, OverlayTexture.DEFAULT_UV, ms, buffer);
		ms.pop();
	}

	@Override
	public ResourceLocation getEntityTexture(PotatoProjectileEntity p_110775_1_) {
		return null;
	}

}
