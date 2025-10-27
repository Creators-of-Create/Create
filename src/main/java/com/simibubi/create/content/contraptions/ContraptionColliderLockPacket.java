package com.simibubi.create.content.contraptions;

import com.simibubi.create.AllPackets;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.net.base.ServerboundPacketPayload;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record ContraptionColliderLockPacket(int contraption, double offset, int sender) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, ContraptionColliderLockPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, ContraptionColliderLockPacket::contraption,
			ByteBufCodecs.DOUBLE, ContraptionColliderLockPacket::offset,
			ByteBufCodecs.VAR_INT, ContraptionColliderLockPacket::sender,
	        ContraptionColliderLockPacket::new
	);

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(LocalPlayer player) {
		ContraptionCollider.lockPacketReceived(contraption, sender, offset);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CONTRAPTION_COLLIDER_LOCK;
	}

	public record ContraptionColliderLockPacketRequest(int contraption, double offset) implements ServerboundPacketPayload {
		public static final StreamCodec<ByteBuf, ContraptionColliderLockPacketRequest> STREAM_CODEC = StreamCodec.composite(
		        ByteBufCodecs.VAR_INT, ContraptionColliderLockPacketRequest::contraption,
				ByteBufCodecs.DOUBLE, ContraptionColliderLockPacketRequest::offset,
		        ContraptionColliderLockPacketRequest::new
		);

		@Override
		public void handle(ServerPlayer player) {
			CatnipServices.NETWORK.sendToClientsTrackingEntity(player, new ContraptionColliderLockPacket(contraption, offset, player.getId()));
		}

		@Override
		public PacketTypeProvider getTypeProvider() {
			return AllPackets.CONTRAPTION_COLLIDER_LOCK_REQUEST;
		}
	}

}
