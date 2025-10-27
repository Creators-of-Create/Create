package com.simibubi.create.content.equipment.bell;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.simibubi.create.AllBlocks;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.data.IntAttached;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@EventBusSubscriber
public class HauntedBellPulser {

	public static final int DISTANCE = 3;
	public static final int RECHARGE_TICKS = 8;
	public static final int WARMUP_TICKS = 10;

	public static final Cache<UUID, IntAttached<Entity>> WARMUP = CacheBuilder.newBuilder()
		.expireAfterAccess(250, TimeUnit.MILLISECONDS)
		.build();

	@SubscribeEvent
	public static void hauntedBellCreatesPulse(PlayerTickEvent.Post event) {
		Player player = event.getEntity();

		if (player.level().isClientSide())
			return;

		if (player.isSpectator())
			return;
		if (!player.isHolding(AllBlocks.HAUNTED_BELL::isIn))
			return;

		boolean firstPulse = false;

		try {
			IntAttached<Entity> ticker = WARMUP.get(player.getUUID(), () -> IntAttached.with(WARMUP_TICKS, player));
			firstPulse = ticker.getFirst() == 1;
			ticker.decrement();
			if (!ticker.isOrBelowZero())
				return;
		} catch (ExecutionException ignored) {}

		long gameTime = player.level().getGameTime();
		if ((firstPulse || gameTime % RECHARGE_TICKS != 0) && player.level() instanceof ServerLevel serverLevel)
			sendPulse(serverLevel, player.blockPosition(), DISTANCE, false);
	}

	public static void sendPulse(ServerLevel world, BlockPos pos, int distance, boolean canOverlap) {
		ChunkPos chunk = world.getChunkAt(pos).getPos();
		CatnipServices.NETWORK.sendToClientsTrackingChunk(world, chunk, new SoulPulseEffectPacket(pos, distance, canOverlap));
	}

}
