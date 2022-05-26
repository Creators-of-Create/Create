package com.simibubi.create.compat.trinkets;

import com.mojang.blaze3d.vertex.PoseStack;

import com.mojang.math.Vector3f;
import com.simibubi.create.AllItems;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class GoggleTrinketRenderer implements TrinketRenderer {
	@Override
	public void render(ItemStack stack, SlotReference slotReference, EntityModel<? extends LivingEntity> model,
					   PoseStack matrices, MultiBufferSource multiBufferSource, int light, LivingEntity entity,
					   float limbAngle, float limbDistance, float tickDelta, float animationProgress,
					   float headYaw, float headPitch) {
		if (AllItems.GOGGLES.isIn(stack) &&
				model instanceof PlayerModel playerModel &&
				entity instanceof AbstractClientPlayer player) {

			// Translate and rotate with our head
			matrices.pushPose();
			TrinketRenderer.followBodyRotations(entity, playerModel);
			TrinketRenderer.translateToFace(matrices, playerModel, player, headYaw, headPitch);

			// Translate and scale to our head
			matrices.translate(0, 0, 0.3);
			matrices.mulPose(Vector3f.ZP.rotationDegrees(180.0f));
			matrices.scale(0.625f, 0.625f, 0.625f);

			if (!entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
				matrices.mulPose(Vector3f.ZP.rotationDegrees(180.0f));
				matrices.translate(0, -0.25, 0);
			}

			// Render
			Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.HEAD,
					light, OverlayTexture.NO_OVERLAY, matrices, multiBufferSource, 0);
			matrices.popPose();
		}
	}
}
