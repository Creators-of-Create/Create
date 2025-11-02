package com.simibubi.create.content.contraptions.sync;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record ContraptionSeatMappingPacket(int entityId, Map<UUID, Integer> mapping, int dismountedId) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, ContraptionSeatMappingPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, ContraptionSeatMappingPacket::entityId,
		ByteBufCodecs.map(HashMap::new, UUIDUtil.STREAM_CODEC, ByteBufCodecs.INT), p -> new HashMap<>(p.mapping),
			ByteBufCodecs.INT, ContraptionSeatMappingPacket::dismountedId,
	        ContraptionSeatMappingPacket::new
	);

	public ContraptionSeatMappingPacket(int entityID, Map<UUID, Integer> mapping) {
		this(entityID, mapping, -1);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(LocalPlayer player) {
		Entity entityByID = player.clientLevel.getEntity(entityId);
		if (!(entityByID instanceof AbstractContraptionEntity contraptionEntity))
			return;

		if (dismountedId == player.getId()) {
			Vec3 transformedVector = contraptionEntity.getPassengerPosition(player, 1);
			if (transformedVector != null)
				player.getPersistentData()
						.put("ContraptionDismountLocation", VecHelper.writeNBT(transformedVector));
		}

		contraptionEntity.getContraption()
			.setSeatMapping(new HashMap<>(mapping));
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CONTRAPTION_SEAT_MAPPING;
	}
}
