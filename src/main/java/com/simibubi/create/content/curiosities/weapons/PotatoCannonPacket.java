package com.simibubi.create.content.curiosities.weapons;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PotatoCannonPacket extends SimplePacketBase {

	public PotatoCannonPacket() {}

	public PotatoCannonPacket(PacketBuffer buffer) {}

	@Override
	public void write(PacketBuffer buffer) {}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> PotatoCannonItem.PREV_SHOT = 15);
		context.get()
			.setPacketHandled(true);
	}

}
