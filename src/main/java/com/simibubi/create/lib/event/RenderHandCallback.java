package com.simibubi.create.lib.event;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public interface RenderHandCallback {
	public static final Event<RenderHandCallback> EVENT = EventFactory.createArrayBacked(RenderHandCallback.class, callbacks -> (player, hand, stack, matrices, vertexConsumers, tickDelta, pitch, swingProgress, equipProgress, light) -> {
		for (RenderHandCallback callback : callbacks) {
			if (callback.onRenderHand(player, hand, stack, matrices, vertexConsumers, tickDelta, pitch, swingProgress, equipProgress, light)) {
				return true;
			}
		}
		return false;
	});

	boolean onRenderHand(AbstractClientPlayer player, InteractionHand hand, ItemStack stack, PoseStack matrices, MultiBufferSource vertexConsumers, float tickDelta, float pitch, float swingProgress, float equipProgress, int light);
}
