package com.simibubi.create.foundation.command;

import net.createmod.catnip.net.ClientboundSimpleActionPacket;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.server.level.ServerPlayer;

public class ToggleDebugCommand extends ConfigureConfigCommand {

	public ToggleDebugCommand() {
		super("rainbowDebug");
	}

	@Override
	protected void sendPacket(ServerPlayer player, String option) {
		CatnipServices.NETWORK.sendToPlayer(
				player,
				new ClientboundSimpleActionPacket("rainbowDebug", option)
		);
	}
}
