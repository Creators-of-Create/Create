package me.pepperbell.simplenetworking;

import net.minecraft.network.FriendlyByteBuf;

public interface Packet {
	void encode(FriendlyByteBuf buf);
}
