package com.simibubi.create.content.contraptions;

import com.simibubi.create.AllPackets;
import net.createmod.catnip.net.base.ClientboundPacketPayload;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;
import io.netty.buffer.ByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record ContraptionBlockChangedPacket(int entityId, BlockPos localPos, BlockState newState) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, ContraptionBlockChangedPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, ContraptionBlockChangedPacket::entityId,
			BlockPos.STREAM_CODEC, ContraptionBlockChangedPacket::localPos,
			CatnipStreamCodecs.BLOCK_STATE, ContraptionBlockChangedPacket::newState,
			ContraptionBlockChangedPacket::new
	);

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(LocalPlayer player) {
		AbstractContraptionEntity.handleBlockChangedPacket(this);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CONTRAPTION_BLOCK_CHANGED;
	}
}
