package com.simibubi.create.content.contraptions.components.structureMovement.elevator;

import java.util.function.Supplier;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
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
	public void handle(Supplier<Context> context) {
		Context ctx = context.get();
		ctx.enqueueWork(() -> {
			ServerPlayer sender = ctx.getSender();
			Entity entityByID = sender.getLevel()
				.getEntity(entityId);
			if (!(entityByID instanceof AbstractContraptionEntity ace))
				return;
			if (!(ace.getContraption()instanceof ElevatorContraption ec))
				return;
			if (ace.distanceToSqr(sender) > 50 * 50)
				return;

			Level level = sender.level;
			ElevatorColumn elevatorColumn = ElevatorColumn.get(level, ec.getGlobalColumn());
			if (!elevatorColumn.contacts.contains(targetY))
				return;

			for (BlockPos otherPos : elevatorColumn.getContacts()) {
				BlockState otherState = level.getBlockState(otherPos);
				if (!AllBlocks.ELEVATOR_CONTACT.has(otherState))
					continue;
				level.setBlock(otherPos, otherState.setValue(ElevatorContactBlock.CALLING, otherPos.getY() == targetY),
					2);
				AllBlocks.ELEVATOR_CONTACT.get()
					.scheduleActivation(level, otherPos);
			}

			elevatorColumn.target(targetY);
			elevatorColumn.markDirty();
		});
		ctx.setPacketHandled(true);
	}

}
