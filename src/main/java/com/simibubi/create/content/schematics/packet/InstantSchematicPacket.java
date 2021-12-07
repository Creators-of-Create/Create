package com.simibubi.create.content.schematics.packet;

import java.util.function.Supplier;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class InstantSchematicPacket extends SimplePacketBase {

	private String name;
	private BlockPos origin;
	private BlockPos bounds;

	public InstantSchematicPacket(String name, BlockPos origin, BlockPos bounds) {
		this.name = name;
		this.origin = origin;
		this.bounds = bounds;
	}

	public InstantSchematicPacket(FriendlyByteBuf buffer) {
		name = buffer.readUtf(32767);
		origin = buffer.readBlockPos();
		bounds = buffer.readBlockPos();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUtf(name);
		buffer.writeBlockPos(origin);
		buffer.writeBlockPos(bounds);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				ServerPlayer player = context.get()
						.getSender();
				if (player == null)
					return;
				Create.SCHEMATIC_RECEIVER.handleInstantSchematic(player, name, player.level, origin, bounds);
			});
		context.get()
			.setPacketHandled(true);
	}

}
