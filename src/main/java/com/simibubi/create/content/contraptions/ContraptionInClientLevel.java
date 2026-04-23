package com.simibubi.create.content.contraptions;

import com.simibubi.create.content.contraptions.pulley.PulleyContraption;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.level.entity.EntityInLevelCallback;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ContraptionInClientLevel implements EntityInLevelCallback {
	EntityInLevelCallback base;
	AbstractContraptionEntity entity;
	final int maxDelayTicks = 2;
	static Map<ContraptionInClientLevel, Integer> delayedRemovals = new HashMap<>();

	ContraptionInClientLevel(EntityInLevelCallback base, AbstractContraptionEntity entity) {
		this.base = base;
		this.entity = entity;
	}

	@Override
	public void onMove() {
		this.base.onMove();
	}

	@Override
	public void onRemove(@NotNull RemovalReason removalReason) {
		if (entity.level().isClientSide && removalReason == RemovalReason.DISCARDED
			&& !delayedRemovals.containsKey(this) && entity.contraption != null) {
			entity.collidingEntities.clear();
			int forced_tick = 0;
			if (entity.getContraption() instanceof PulleyContraption)
				forced_tick = 2;
			delayedRemovals.put(this, -forced_tick);
		} else {
			this.base.onRemove(removalReason);
		}
	}

	public void remove() {
		this.base.onRemove(RemovalReason.DISCARDED);
	}

	public static void tick() {
		delayedRemovals.entrySet().removeIf(entry -> {
			ContraptionInClientLevel callback = entry.getKey();
			int ticks = entry.getValue();
			entry.setValue(ticks + 1);
			if (ticks < 0)
				return false;

			AbstractContraptionEntity e = callback.entity;
			ClientContraptionStatus status = e.getClientContraptionStatus();
			if (ticks >= callback.maxDelayTicks || status == ClientContraptionStatus.DO_REMOVE) {
				callback.remove();
				return true;
			}
			if (status == ClientContraptionStatus.ALIVE)
				e.setClientContraptionStatus(ClientContraptionStatus.MARKED_FOR_REMOVAL);
			return false;
		});
	}
}
