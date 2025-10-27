package com.simibubi.create.content.logistics.depot;

import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public class EjectorTriggerPacket extends BlockEntityConfigurationPacket<EjectorBlockEntity> {
	public static final StreamCodec<ByteBuf, EjectorTriggerPacket> STREAM_CODEC = BlockPos.STREAM_CODEC.map(
			EjectorTriggerPacket::new, packet -> packet.pos
	);

	public EjectorTriggerPacket(BlockPos pos) {
		super(pos);
	}

	@Override
	protected void applySettings(ServerPlayer player, EjectorBlockEntity be) {
		be.activate();
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.TRIGGER_EJECTOR;
	}
}
