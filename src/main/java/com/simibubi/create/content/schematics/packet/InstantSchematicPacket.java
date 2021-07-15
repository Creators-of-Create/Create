package com.simibubi.create.content.schematics.packet;

import java.util.function.Supplier;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class InstantSchematicPacket extends SimplePacketBase {

	private String name;
	private BlockPos origin;
	private BlockPos bounds;

	public InstantSchematicPacket(String name, BlockPos origin, BlockPos bounds) {
		this.name = name;
		this.origin = origin;
		this.bounds = bounds;
	}

	public InstantSchematicPacket(PacketBuffer buffer) {
		name = buffer.readUtf(32767);
		origin = buffer.readBlockPos();
		bounds = buffer.readBlockPos();
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeUtf(name);
		buffer.writeBlockPos(origin);
		buffer.writeBlockPos(bounds);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				ServerPlayerEntity player = context.get()
						.getSender();
				if (player == null)
					return;
				Create.SCHEMATIC_RECEIVER.handleInstantSchematic(player, name, player.level, origin, bounds);
			});
		context.get()
			.setPacketHandled(true);
	}

}
