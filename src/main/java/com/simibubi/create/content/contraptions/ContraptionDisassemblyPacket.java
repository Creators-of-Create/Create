package com.simibubi.create.content.contraptions;

import com.simibubi.create.AllPackets;
import net.createmod.catnip.net.base.ClientboundPacketPayload;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import io.netty.buffer.ByteBuf;
import net.neoforged.api.distmarker.Dist;

public record ContraptionDisassemblyPacket(int entityId, StructureTransform transform) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, ContraptionDisassemblyPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, ContraptionDisassemblyPacket::entityId,
			StructureTransform.STREAM_CODEC, ContraptionDisassemblyPacket::transform,
			ContraptionDisassemblyPacket::new
	);

	@Override
	public void handle(Player player) {
		AbstractContraptionEntity.handleDisassemblyPacket(this);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CONTRAPTION_DISASSEMBLE;
	}
}
