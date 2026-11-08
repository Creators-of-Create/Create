package com.simibubi.create.content.contraptions.actors.seat;

import java.util.Map.Entry;
import java.util.UUID;

import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SeatInteractionBehaviour extends MovingInteractionBehaviour {

	@Override
	public boolean handlePlayerInteraction(Player player, InteractionHand hand, BlockPos localPos,
										   AbstractContraptionEntity contraptionEntity) {
		Contraption contraption = contraptionEntity.getContraption();
		int index = contraption.getSeats()
						.indexOf(localPos);
		if (index == -1)
			return false;

		if (player.isPassenger())
			return false;

		Level level = contraptionEntity.level();
		Entity toDismount = null;
		for (Entry<UUID, Integer> entry : contraption.getSeatMapping().entrySet()) {
			if (entry.getValue() != index)
				continue;
			for (Entity entity : contraptionEntity.getPassengers()) {
				if (!entry.getKey().equals(entity.getUUID()))
					continue;
				if (entity instanceof Player)
					return false;
				toDismount = entity;
			}
		}

		if (toDismount != null && !level.isClientSide) {
			Vec3 transformedVector = contraptionEntity.getPassengerPosition(toDismount, 1);
			toDismount.stopRiding();
			if (transformedVector != null)
				toDismount.teleportTo(transformedVector.x, transformedVector.y, transformedVector.z);
		}

		if (level.isClientSide)
			return true;
		contraptionEntity.addSittingPassenger(SeatBlock.getLeashed(level, player).or(player), index);
		return true;
	}

	@Override
	public void handleEntityCollision(Entity entity, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
		Contraption contraption = contraptionEntity.getContraption();
		int index = contraption.getSeats()
			.indexOf(localPos);
		if (index == -1)
			return;
		if (!SeatBlock.canBePickedUp(entity))
			return;
		contraptionEntity.addSittingPassenger(entity, index);
	}

}
