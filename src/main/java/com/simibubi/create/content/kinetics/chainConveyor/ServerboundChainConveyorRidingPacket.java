package com.simibubi.create.content.kinetics.chainConveyor;

import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import com.simibubi.create.infrastructure.config.AllConfigs;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public class ServerboundChainConveyorRidingPacket extends BlockEntityConfigurationPacket<ChainConveyorBlockEntity> {
	public static final StreamCodec<ByteBuf, ServerboundChainConveyorRidingPacket> STREAM_CODEC = StreamCodec.composite(
	    BlockPos.STREAM_CODEC, packet -> packet.pos,
		ByteBufCodecs.BOOL, packet -> packet.stop,
	    ServerboundChainConveyorRidingPacket::new
	);

	private final boolean stop;

	public ServerboundChainConveyorRidingPacket(BlockPos pos, boolean stop) {
		super(pos);
		this.stop = stop;
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CHAIN_CONVEYOR_RIDING;
	}

	@Override
	protected int maxRange() {
		return AllConfigs.server().kinetics.maxChainConveyorLength.get() * 2;
	}

	@Override
	protected void applySettings(ServerPlayer sender, ChainConveyorBlockEntity be) {
		sender.fallDistance = 0;
		sender.connection.aboveGroundTickCount = 0;
		sender.connection.aboveGroundVehicleTickCount = 0;

		if (stop)
			ServerChainConveyorHandler.handleStopRidingPacket(sender);
		else
			ServerChainConveyorHandler.handleTTLPacket(sender);
	}
}
