package com.simibubi.create.content.logistics.trains.entity;

import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityEvent;

/**
 * Removes all Carriage entities in chunks that aren't ticking
 */
public class CarriageEntityHandler {

	public static void onEntityEnterSection(EntityEvent.EnteringSection event) {
		if (!event.didChunkChange())
			return;
		Entity entity = event.getEntity();
		if (!(entity instanceof CarriageContraptionEntity cce))
			return;
		SectionPos newPos = event.getNewPos();
		Level level = entity.getLevel();
		if (level.isClientSide)
			return;
		if (!isActiveChunk(level, newPos.chunk()))
			cce.leftTickingChunks = true;
	}

	public static void validateCarriageEntity(CarriageContraptionEntity entity) {
		if (!entity.isAlive())
			return;
		Level level = entity.getLevel();
		if (level.isClientSide)
			return;
		if (!isActiveChunk(level, entity.chunkPosition()))
			entity.leftTickingChunks = true;
	}

	public static boolean isActiveChunk(Level level, ChunkPos chunk) {
		if (level instanceof ServerLevel serverLevel)
			return serverLevel.isPositionEntityTicking(chunk);
		return false;
	}

}
