package com.simibubi.create.content.curiosities.bell;

import java.util.function.Supplier;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class SoulPulseEffectPacket extends SimplePacketBase {

	public BlockPos pos;
	public int distance;
	public boolean canOverlap;

	public SoulPulseEffectPacket(BlockPos pos, int distance, boolean overlaps) {
		this.pos = pos;
		this.distance = distance;
		this.canOverlap = overlaps;
	}

	public SoulPulseEffectPacket(PacketBuffer buffer) {
		pos = buffer.readBlockPos();
		distance = buffer.readInt();
		canOverlap = buffer.readBoolean();
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeInt(distance);
		buffer.writeBoolean(canOverlap);
	}

	@Override
	public void handle(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> {
			CreateClient.SOUL_PULSE_EFFECT_HANDLER.addPulse(new SoulPulseEffect(pos, distance, canOverlap));
		});
		context.get().setPacketHandled(true);
	}

}
