package com.simibubi.create.lib.event;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public interface RenderHandCallback {
	Event<RenderHandCallback> EVENT = EventFactory.createArrayBacked(RenderHandCallback.class, callbacks -> (handEvent) -> {
		for (RenderHandCallback callback : callbacks) {
			callback.onRenderHand(handEvent);
		}
	});

	void onRenderHand(RenderHandEvent event);

	class RenderHandEvent extends CancellableEvent {
		private AbstractClientPlayer player;
		private InteractionHand hand;
		private ItemStack stack;
		private PoseStack matrices;
		private MultiBufferSource vertexConsumers;
		private float tickDelta;
		private float pitch;
		private float swingProgress;
		private float equipProgress;
		private int light;

		public RenderHandEvent(AbstractClientPlayer player, InteractionHand hand, ItemStack stack, PoseStack matrices, MultiBufferSource vertexConsumers, float tickDelta, float pitch, float swingProgress, float equipProgress, int light) {
			this.player = player;
			this.hand = hand;
			this.stack = stack;
			this.matrices = matrices;
			this.vertexConsumers = vertexConsumers;
			this.tickDelta = tickDelta;
			this.pitch = pitch;
			this.swingProgress = swingProgress;
			this.equipProgress = equipProgress;
			this.light = light;
		}

		public ItemStack getItemStack() {
			return stack;
		}

		public PoseStack getMatrixStack() {
			return matrices;
		}

		public MultiBufferSource getBuffers() {
			return vertexConsumers;
		}

		public int getLight() {
			return light;
		}

		public float getPartialTicks() {
			return tickDelta;
		}

		public InteractionHand getHand() {
			return hand;
		}

		public float getEquipProgress() {
			return equipProgress;
		}

		public float getSwingProgress() {
			return swingProgress;
		}
	}
}
