package com.simibubi.create.foundation.gui;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class ClearContainerPacket extends SimplePacketBase {

	public ClearContainerPacket() {}

	public ClearContainerPacket(PacketBuffer buffer) {}

	@Override
	public void write(PacketBuffer buffer) {}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				ServerPlayerEntity player = context.get()
					.getSender();
				if (player == null)
					return;
				if (!(player.openContainer instanceof IClearableContainer))
					return;
				((IClearableContainer) player.openContainer).clearContents();
			});
		context.get()
			.setPacketHandled(true);
	}

}
