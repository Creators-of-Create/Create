package com.simibubi.create.content.contraptions.glue;

import com.simibubi.create.AllPackets;
import net.createmod.catnip.net.base.ClientboundPacketPayload;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;

public record GlueEffectPacket(BlockPos pos, Direction direction, boolean fullBlock) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, GlueEffectPacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, GlueEffectPacket::pos,
			Direction.STREAM_CODEC, GlueEffectPacket::direction,
			ByteBufCodecs.BOOL, GlueEffectPacket::fullBlock,
			GlueEffectPacket::new
	);

	@Override
	public void handle(Player player) {
		if (!player.blockPosition().closerThan(pos, 100))
			return;
		SuperGlueItem.spawnParticles(player.level(), pos, direction, fullBlock);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.GLUE_EFFECT;
	}
}
