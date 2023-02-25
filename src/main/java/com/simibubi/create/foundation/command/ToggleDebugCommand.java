package com.simibubi.create.foundation.command;

import com.simibubi.create.foundation.networking.AllPackets;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public class ToggleDebugCommand extends ConfigureConfigCommand {

	public ToggleDebugCommand() {
		super("rainbowDebug");
	}

	@Override
	protected void sendPacket(ServerPlayer player, String option) {
		AllPackets.getChannel().send(
				PacketDistributor.PLAYER.with(() -> player),
				new SConfigureConfigPacket(SConfigureConfigPacket.Actions.rainbowDebug.name(), option)
		);
	}
}
