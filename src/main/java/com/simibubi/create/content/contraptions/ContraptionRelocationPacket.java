package com.simibubi.create.content.contraptions;

import com.simibubi.create.AllPackets;
import net.createmod.catnip.net.base.ClientboundPacketPayload;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;

public record ContraptionRelocationPacket(int entityId) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, ContraptionRelocationPacket> STREAM_CODEC = ByteBufCodecs.INT.map(
			ContraptionRelocationPacket::new, ContraptionRelocationPacket::entityId
	);

	@Override
	public void handle(Player player) {
		OrientedContraptionEntity.handleRelocationPacket(this);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CONTRAPTION_RELOCATION;
	}
}
