package com.simibubi.create.foundation.command;

import com.simibubi.create.foundation.networking.AllPackets;

import net.minecraft.server.level.ServerPlayer;

public class ToggleDebugCommand extends ConfigureConfigCommand {

	public ToggleDebugCommand() {
		super("rainbowDebug");
	}

	@Override
	protected void sendPacket(ServerPlayer player, String option) {
		AllPackets.channel.sendToClient(new SConfigureConfigPacket(SConfigureConfigPacket.Actions.rainbowDebug.name(), option), player);
	}
}
