package com.simibubi.create.foundation.utility;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * A manager to handle extraction reservations for specific block positions.
 * This ensures that only one blockEntity can extract items from a specific block position at a time per tick.
 *
 * This is done via a simple locking system, calling tryLock will return false if the block is already locked or true if it manages to successfully lock the block, at the start of every server tick the current locks are cleared
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
