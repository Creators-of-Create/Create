package com.simibubi.create.content.kinetics.chainConveyor;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.render.PlayerSkyhookRenderer;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import net.neoforged.api.distmarker.Dist;

public record ClientboundChainConveyorRidingPacket(Collection<UUID> uuids) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, ClientboundChainConveyorRidingPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.collection(HashSet::new, UUIDUtil.STREAM_CODEC), ClientboundChainConveyorRidingPacket::uuids,
	    ClientboundChainConveyorRidingPacket::new
	);

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CLIENTBOUND_CHAIN_CONVEYOR;
	}

	@Override
	public void handle(Player player) {
		PlayerSkyhookRenderer.updatePlayerList(this.uuids);
	}
}
