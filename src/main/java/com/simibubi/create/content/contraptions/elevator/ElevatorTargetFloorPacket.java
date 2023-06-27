package com.simibubi.create.content.contraptions.elevator;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent.Context;

public class ElevatorTargetFloorPacket extends SimplePacketBase {

	private int entityId;
	private int targetY;

	public ElevatorTargetFloorPacket(AbstractContraptionEntity entity, int targetY) {
		this.targetY = targetY;
		this.entityId = entity.getId();
	}

	public ElevatorTargetFloorPacket(FriendlyByteBuf buffer) {
		entityId = buffer.readInt();
		targetY = buffer.readInt();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(entityId);
		buffer.writeInt(targetY);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer sender = context.getSender();
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
		});
		return true;
	}

}
