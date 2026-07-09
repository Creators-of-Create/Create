package com.simibubi.create.content.contraptions;

import com.simibubi.create.AllPackets;
import net.createmod.catnip.net.base.ClientboundPacketPayload;

import net.createmod.catnip.api.data.codec.stream.CatnipStreamCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;
import io.netty.buffer.ByteBuf;
import net.neoforged.api.distmarker.Dist;

public record ContraptionBlockChangedPacket(int entityId, BlockPos localPos, BlockState newState) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, ContraptionBlockChangedPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, ContraptionBlockChangedPacket::entityId,
			BlockPos.STREAM_CODEC, ContraptionBlockChangedPacket::localPos,
			CatnipStreamCodecs.BLOCK_STATE, ContraptionBlockChangedPacket::newState,
			ContraptionBlockChangedPacket::new
	);

	@Override
	public void handle(Player player) {
		AbstractContraptionEntity.handleBlockChangedPacket(this);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CONTRAPTION_BLOCK_CHANGED;
	}
}
