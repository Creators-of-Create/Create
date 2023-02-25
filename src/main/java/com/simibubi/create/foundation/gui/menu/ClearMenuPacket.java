package com.simibubi.create.foundation.gui.menu;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class ClearMenuPacket extends SimplePacketBase {

	public ClearMenuPacket() {}

	public ClearMenuPacket(FriendlyByteBuf buffer) {}

	@Override
	public void write(FriendlyByteBuf buffer) {}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				ServerPlayer player = context.get()
					.getSender();
				if (player == null)
					return;
				if (!(player.containerMenu instanceof IClearableMenu))
					return;
				((IClearableMenu) player.containerMenu).clearContents();
			});
		context.get()
			.setPacketHandled(true);
	}

}
