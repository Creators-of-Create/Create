package com.simibubi.create.content.equipment.potatoCannon;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class PotatoProjectileRenderer extends EntityRenderer<PotatoProjectileEntity, EntityRenderState> {

	public PotatoProjectileRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	public void render(PotatoProjectileEntity entity, float yaw, float pt, PoseStack ms, MultiBufferSource buffer,
		int light) {
		// TODO 26.2: Port potato projectile item rendering to EntityRenderer#submit.
	}

	@Override
	public EntityRenderState createRenderState() {
		return new EntityRenderState();
	}
}
