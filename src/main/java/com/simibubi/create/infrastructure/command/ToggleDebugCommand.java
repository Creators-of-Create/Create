package com.simibubi.create.infrastructure.command;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.server.level.ServerPlayer;

public class ToggleDebugCommand extends ConfigureConfigCommand {

	public ToggleDebugCommand() {
		super("rainbowDebug");
	}

	@Override
	protected void sendPacket(ServerPlayer player, String option) {
		CatnipServices.NETWORK.simpleActionToClient(player, "rainbowDebug", option);
	}
}
