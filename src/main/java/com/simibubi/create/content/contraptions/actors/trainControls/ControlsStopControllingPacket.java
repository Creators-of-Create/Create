package com.simibubi.create.content.contraptions.actors.trainControls;

import com.simibubi.create.AllPackets;
import net.createmod.catnip.net.base.ClientboundPacketPayload;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;

public enum ControlsStopControllingPacket implements ClientboundPacketPayload {
	INSTANCE;

	public static final StreamCodec<ByteBuf, ControlsStopControllingPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public void handle(Player player) {
		ControlsHandler.stopControlling();
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CONTROLS_ABORT;
	}
}
