package com.simibubi.create.content.logistics.depot;

import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public class EjectorAwardPacket extends BlockEntityConfigurationPacket<EjectorBlockEntity> {
	public static final StreamCodec<ByteBuf, EjectorAwardPacket> STREAM_CODEC = BlockPos.STREAM_CODEC.map(
			EjectorAwardPacket::new, packet -> packet.pos
	);

	public EjectorAwardPacket(BlockPos pos) {
		super(pos);
	}

	@Override
	protected void applySettings(ServerPlayer player, EjectorBlockEntity be) {
		AllAdvancements.EJECTOR_MAXED.awardTo(player);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.EJECTOR_AWARD;
	}
}
