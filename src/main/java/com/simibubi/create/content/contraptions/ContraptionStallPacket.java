package com.simibubi.create.content.contraptions;

import com.simibubi.create.AllPackets;
import net.createmod.catnip.net.base.ClientboundPacketPayload;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record ContraptionStallPacket(int entityId, double x, double y, double z, float angle) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, ContraptionStallPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, ContraptionStallPacket::entityId,
			ByteBufCodecs.DOUBLE, ContraptionStallPacket::x,
			ByteBufCodecs.DOUBLE, ContraptionStallPacket::y,
			ByteBufCodecs.DOUBLE, ContraptionStallPacket::z,
			ByteBufCodecs.FLOAT, ContraptionStallPacket::angle,
			ContraptionStallPacket::new
	);

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(LocalPlayer player) {
		AbstractContraptionEntity.handleStallPacket(this);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CONTRAPTION_STALL;
	}
}
