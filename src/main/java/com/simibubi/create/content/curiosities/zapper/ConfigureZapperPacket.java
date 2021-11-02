package com.simibubi.create.content.curiosities.zapper;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public abstract class ConfigureZapperPacket extends SimplePacketBase {

	protected Hand hand;
	protected PlacementPatterns pattern;

	public ConfigureZapperPacket(Hand hand, PlacementPatterns pattern) {
		this.hand = hand;
		this.pattern = pattern;
	}

	public ConfigureZapperPacket(PacketBuffer buffer) {
		hand = buffer.readEnum(Hand.class);
		pattern = buffer.readEnum(PlacementPatterns.class);
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeEnum(hand);
		buffer.writeEnum(pattern);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayerEntity player = context.get().getSender();
			if (player == null) {
				return;
			}
			ItemStack stack = player.getItemInHand(hand);
			if (stack.getItem() instanceof ZapperItem) {
				configureZapper(stack);
			}
		});
		context.get().setPacketHandled(true);
	}

	public abstract void configureZapper(ItemStack stack);

}
