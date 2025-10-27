package com.simibubi.create.content.contraptions.elevator;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.createmod.catnip.net.base.ServerboundPacketPayload;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public record ElevatorTargetFloorPacket(int entityId, int targetY) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, ElevatorTargetFloorPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, ElevatorTargetFloorPacket::entityId,
			ByteBufCodecs.INT, ElevatorTargetFloorPacket::targetY,
	        ElevatorTargetFloorPacket::new
	);

	public ElevatorTargetFloorPacket(AbstractContraptionEntity entity, int targetY) {
		this(entity.getId(), targetY);
	}

	@Override
	public void handle(ServerPlayer sender) {
		Entity entityByID = sender.serverLevel()
				.getEntity(entityId);
		if (!(entityByID instanceof AbstractContraptionEntity ace))
			return;
		if (!(ace.getContraption() instanceof ElevatorContraption ec))
			return;
		if (ace.distanceToSqr(sender) > 50 * 50)
			return;

		Level level = sender.level();
		ElevatorColumn elevatorColumn = ElevatorColumn.get(level, ec.getGlobalColumn());
		if (!elevatorColumn.contacts.contains(targetY))
			return;
		if (ec.isTargetUnreachable(targetY))
			return;

		BlockPos pos = elevatorColumn.contactAt(targetY);
		BlockState blockState = level.getBlockState(pos);
		if (!(blockState.getBlock() instanceof ElevatorContactBlock ecb))
			return;

		ecb.callToContactAndUpdate(elevatorColumn, blockState, level, pos, false);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.ELEVATOR_SET_FLOOR;
	}
}
