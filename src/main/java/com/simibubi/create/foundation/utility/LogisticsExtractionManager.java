package com.simibubi.create.foundation.utility;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * A manager to handle extraction reservations for specific block positions.
 * This ensures that only one entity can extract items from a specific block position at a time per tick.
 *
 * <p>The `LogisticsExtractionManager` class is responsible for managing and coordinating
 * extraction operations in the game, preventing multiple entities from extracting
 * items from the same block position simultaneously within a single tick. It achieves
 * this by maintaining a set of reserved block positions and providing methods to
 * reserve and release these positions.</p>
 *
 * <p>Key responsibilities of this class include:</p>
 * <ul>
 *   <li>Reserving block positions for extraction to ensure that only one entity can perform an extraction operation on a specific position per tick.</li>
 *   <li>Releasing reservations for block positions once the extraction operation is complete.</li>
 *   <li>Clearing all reserved positions at the start of each tick to reset the state and prepare for the next round of extraction operations.</li>
 * </ul>
 *
 * <p>This class is essential for preventing issues such as duplicated item extraction,
 * ensuring the game's logistics and extraction mechanisms function smoothly and correctly.</p>
 */
@Mod.EventBusSubscriber
public class LogisticsExtractionManager {

	/**
	 * A set to store reserved block positions while tick processing
	 */
	private static final Set<BlockPos> lockedPositions = new HashSet<>();

	/**
	 * Tries to lock the specified block position.
	 * If the position is already locked, the method returns false.
	 *
	 * @param pos The block position to lock.
	 * @return True if the position was successfully locked, false otherwise.
	 */
	public static boolean tryLock(BlockPos pos) {
		if (lockedPositions.contains(pos)) {
			return false;
		} else {
			lockedPositions.add(pos);
			return true;
		}
	}

	/**
	 * Clears all locked positions at the start of each tick.
	 *
	 * @param event The server tick event.
	 */
	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			lockedPositions.clear();
		}
	}

}
