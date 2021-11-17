package com.simibubi.create.content.curiosities.bell;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.IntAttached;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class HauntedBellPulser {

	public static final int DISTANCE = 3;
	public static final int RECHARGE_TICKS = 8;
	public static final int WARMUP_TICKS = 10;

	public static final Cache<UUID, IntAttached<Entity>> WARMUP = CacheBuilder.newBuilder()
		.expireAfterAccess(250, TimeUnit.MILLISECONDS)
		.build();

	public static void hauntedBellCreatesPulse(Player player) {
//		if (event.phase != TickEvent.Phase.END)
//			return;
//		if (event.side != LogicalSide.SERVER)
//			return;
		if (player.level.isClientSide())
			return;
		if (player.isSpectator())
			return;
		if (!player.isHolding(AllBlocks.HAUNTED_BELL::isIn))
			return;

//		Entity player = event.player;
		boolean firstPulse = false;

		try {
			IntAttached<Entity> ticker = WARMUP.get(player.getUUID(), () -> IntAttached.with(WARMUP_TICKS, player));
			firstPulse = ticker.getFirst()
				.intValue() == 1;
			ticker.decrement();
			if (!ticker.isOrBelowZero())
				return;
		} catch (ExecutionException e) {
		}

		long gameTime = player.level.getGameTime();
		if (firstPulse || gameTime % RECHARGE_TICKS != 0)
			sendPulse(player.level, player.blockPosition(), DISTANCE, false);
	}

	public static void sendPulse(Level world, BlockPos pos, int distance, boolean canOverlap) {
//		LevelChunk chunk = world.getChunkAt(pos);
		AllPackets.channel.sendToClientsTracking(new SoulPulseEffectPacket(pos, distance, canOverlap), (ServerLevel) world, pos);
	}

}
