package com.simibubi.create.content.contraptions.actors.seat;

import java.util.Map;
import java.util.UUID;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;

import net.createmod.catnip.utility.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;

public class SeatMovementBehaviour implements MovementBehaviour {

	@Override
	public void startMoving(MovementContext context) {
		MovementBehaviour.super.startMoving(context);
		int indexOf = context.contraption.getSeats()
			.indexOf(context.localPos);
		context.data.putInt("SeatIndex", indexOf);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		MovementBehaviour.super.visitNewPosition(context, pos);

		AbstractContraptionEntity contraptionEntity = context.contraption.entity;
		if (contraptionEntity == null)
			return;
		int index = context.data.getInt("SeatIndex");
		if (index == -1)
			return;

		Map<UUID, Integer> seatMapping = context.contraption.getSeatMapping();
		BlockState blockState = context.world.getBlockState(pos);
		boolean slab =
			blockState.getBlock() instanceof SlabBlock && blockState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
		boolean solid = blockState.canOcclude() || slab;

		// Occupied
		if (!seatMapping.containsValue(index))
			return;
		if (!solid)
			return;
		Entity toDismount = null;
		for (Map.Entry<UUID, Integer> entry : seatMapping.entrySet()) {
			if (entry.getValue() != index)
				continue;
			for (Entity entity : contraptionEntity.getPassengers()) {
				if (!entry.getKey()
					.equals(entity.getUUID()))
					continue;
				toDismount = entity;
			}
		}
		if (toDismount == null)
			return;
		toDismount.stopRiding();
		Vec3 position = VecHelper.getCenterOf(pos)
			.add(0, slab ? .5f : 1f, 0);
		toDismount.teleportTo(position.x, position.y, position.z);
		toDismount.getPersistentData()
			.remove("ContraptionDismountLocation");
	}

}
