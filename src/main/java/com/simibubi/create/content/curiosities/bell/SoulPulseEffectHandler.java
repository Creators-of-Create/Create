package com.simibubi.create.content.curiosities.bell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class SoulPulseEffectHandler {

	private List<SoulPulseEffect> pulses;
	private Set<BlockPos> occupied;

	public SoulPulseEffectHandler() {
		pulses = new ArrayList<>();
		occupied = new HashSet<>();
	}

	public void tick(World world) {
		for (SoulPulseEffect pulse : pulses) {
			List<BlockPos> added = pulse.tick(world);
			if (added == null)
				continue;

			if (pulse.overlaps()) {
				for (BlockPos pos : added) {
					SoulPulseEffect.spawnParticles(world, pos);
				}
			} else {
				for (BlockPos pos : added) {
					if (occupied.contains(pos))
						continue;
					SoulPulseEffect.spawnParticles(world, pos);
					occupied.add(pos);
				}
			}
		}

		Map<Boolean, List<SoulPulseEffect>> split = pulses.stream()
				.collect(Collectors.partitioningBy(SoulPulseEffect::finished));
		for (SoulPulseEffect finished : split.get(true))
			finished.removeAdded(occupied);
		pulses = split.get(false);
	}

	public void refresh() {
		pulses.clear();
		occupied.clear();
	}

	public static void sendPulsePacket(World world, BlockPos at, int distance, boolean overlaps) {
		Chunk chunk = world.getChunkAt(at);
		AllPackets.channel.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), new Packet(at, distance, overlaps));
	}

	private void handlePulse(BlockPos pos, int distance, boolean overlaps) {
		pulses.add(new SoulPulseEffect(pos, distance, overlaps));
	}


	public static class Packet extends SimplePacketBase {
		public BlockPos pos;
		public int distance;
		public boolean overlaps;

		public Packet(BlockPos pos, int distance, boolean overlaps) {
			this.pos = pos;
			this.distance = distance;
			this.overlaps = overlaps;
		}

		public Packet(PacketBuffer buffer) {
			pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
			distance = buffer.readInt();
			overlaps = buffer.readBoolean();
		}

		@Override
		public void write(PacketBuffer buffer) {
			buffer.writeInt(pos.getX());
			buffer.writeInt(pos.getY());
			buffer.writeInt(pos.getZ());
			buffer.writeInt(distance);
			buffer.writeBoolean(overlaps);
		}

		@Override
		public void handle(Supplier<NetworkEvent.Context> context) {
			context.get().enqueueWork(() -> CreateClient.SOUL_PULSE_EFFECT_HANDLER.handlePulse(pos, distance, overlaps));
			context.get().setPacketHandled(true);
		}
	}

}
