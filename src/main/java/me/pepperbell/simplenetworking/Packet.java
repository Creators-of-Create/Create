package me.pepperbell.simplenetworking;

import net.minecraft.network.FriendlyByteBuf;

public interface Packet {
	void write(FriendlyByteBuf buf);
}
