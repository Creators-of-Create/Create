package com.simibubi.create.foundation.gui.menu;

import com.simibubi.create.AllPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public enum ClearMenuPacket implements ServerboundPacketPayload {
	INSTANCE;

	public static final StreamCodec<ByteBuf, ClearMenuPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CLEAR_CONTAINER;
	}

	@Override
	public void handle(ServerPlayer player) {
		if (player == null)
			return;
		if (!(player.containerMenu instanceof IClearableMenu))
			return;
		((IClearableMenu) player.containerMenu).clearContents();
	}
}
