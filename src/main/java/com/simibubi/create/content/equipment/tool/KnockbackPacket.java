package com.simibubi.create.content.equipment.tool;

import com.simibubi.create.AllPackets;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import net.neoforged.api.distmarker.Dist;

public record KnockbackPacket(float yRot, float strength) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, KnockbackPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.FLOAT, KnockbackPacket::yRot,
	    ByteBufCodecs.FLOAT, KnockbackPacket::strength,
	    KnockbackPacket::new
	);

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.KNOCKBACK;
	}

	@Override
	public void handle(Player player) {
		if (player != null)
			CardboardSwordItem.knockback(player, strength, yRot);
	}
}
