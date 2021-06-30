package com.simibubi.create.content.curiosities.bell;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.networking.AllPackets;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.network.PacketDistributor;

@EventBusSubscriber
public class CursedBellPulser {

	public static final int DISTANCE = 3;
	public static final int RECHARGE_TICKS = 8;

	@SubscribeEvent
	public static void cursedBellCreatesPulse(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.END)
			return;
		if (event.side != LogicalSide.SERVER)
			return;
		if (event.player.isSpectator())
			return;

		if (event.player.world.getGameTime() % RECHARGE_TICKS != 0)
			return;

		if (event.player.isHolding(AllBlocks.CURSED_BELL::is))
			sendPulse(event.player.world, event.player.getBlockPos(), DISTANCE, false);
	}

	public static void sendPulse(World world, BlockPos pos, int distance, boolean canOverlap) {
		Chunk chunk = world.getChunkAt(pos);
		AllPackets.channel.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), new SoulPulseEffectPacket(pos, distance, canOverlap));
	}

}
