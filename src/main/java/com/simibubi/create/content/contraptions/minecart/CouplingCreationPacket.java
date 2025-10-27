package com.simibubi.create.content.contraptions.minecart;

import com.simibubi.create.AllPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.vehicle.AbstractMinecart;

public record CouplingCreationPacket(int id1, int id2) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, CouplingCreationPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, CouplingCreationPacket::id1,
			ByteBufCodecs.VAR_INT, CouplingCreationPacket::id2,
			CouplingCreationPacket::new
	);

	public CouplingCreationPacket(AbstractMinecart cart1, AbstractMinecart cart2) {
		this(cart1.getId(), cart2.getId());
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.MINECART_COUPLING_CREATION;
	}

	@Override
	public void handle(ServerPlayer player) {
		CouplingHandler.tryToCoupleCarts(player, player.level(), id1, id2);
	}
}
