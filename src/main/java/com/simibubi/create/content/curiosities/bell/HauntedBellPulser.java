package com.simibubi.create.content.curiosities.bell;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.IntAttached;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.network.PacketDistributor;

@EventBusSubscriber
public class HauntedBellPulser {

	public static final int DISTANCE = 3;
	public static final int RECHARGE_TICKS = 8;
	public static final int WARMUP_TICKS = 10;

	public static final Cache<UUID, IntAttached<Entity>> WARMUP = CacheBuilder.newBuilder()
		.expireAfterAccess(250, TimeUnit.MILLISECONDS)
		.build();

	@SubscribeEvent
	public static void hauntedBellCreatesPulse(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.END)
			return;
		if (event.side != LogicalSide.SERVER)
			return;
		if (event.player.isSpectator())
			return;
		if (!event.player.isHolding(AllBlocks.HAUNTED_BELL::is))
			return;

		Entity player = event.player;
		boolean firstPulse = false;

		try {
			IntAttached<Entity> ticker = WARMUP.get(player.getUniqueID(), () -> IntAttached.with(WARMUP_TICKS, player));
			firstPulse = ticker.getFirst()
				.intValue() == 1;
			ticker.decrement();
			if (!ticker.isOrBelowZero())
				return;
		} catch (ExecutionException e) {
		}

		long gameTime = player.world.getGameTime();
		if (firstPulse || gameTime % RECHARGE_TICKS != 0)
			sendPulse(player.world, event.player.getBlockPos(), DISTANCE, false);
	}

	public static void sendPulse(World world, BlockPos pos, int distance, boolean canOverlap) {
		Chunk chunk = world.getChunkAt(pos);
		AllPackets.channel.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk),
			new SoulPulseEffectPacket(pos, distance, canOverlap));
	}

}
