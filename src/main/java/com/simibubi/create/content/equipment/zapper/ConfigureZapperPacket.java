package com.simibubi.create.content.equipment.zapper;

import net.createmod.catnip.net.base.ServerboundPacketPayload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public abstract class ConfigureZapperPacket implements ServerboundPacketPayload {
	protected final InteractionHand hand;
	protected final PlacementPatterns pattern;

	public ConfigureZapperPacket(InteractionHand hand, PlacementPatterns pattern) {
		this.hand = hand;
		this.pattern = pattern;
	}

	@Override
	public void handle(ServerPlayer player) {
		ItemStack stack = player.getItemInHand(hand);
		if (stack.getItem() instanceof ZapperItem) {
			configureZapper(stack);
		}
	}

	public abstract void configureZapper(ItemStack stack);

}
