package me.pepperbell.simplenetworking;

import net.minecraft.network.FriendlyByteBuf;

public interface Packet {
	void read(FriendlyByteBuf buf);

	void write(FriendlyByteBuf buf);
}
