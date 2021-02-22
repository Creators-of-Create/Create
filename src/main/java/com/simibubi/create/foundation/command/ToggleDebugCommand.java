package com.simibubi.create.foundation.command;

import com.simibubi.create.foundation.networking.AllPackets;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;

public class ToggleDebugCommand extends ConfigureConfigCommand {

	public ToggleDebugCommand() {
		super("rainbowDebug");
	}

	@Override
	protected void sendPacket(ServerPlayerEntity player, String option) {
		AllPackets.channel.send(
				PacketDistributor.PLAYER.with(() -> player),
				new ConfigureConfigPacket(ConfigureConfigPacket.Actions.rainbowDebug.name(), option)
		);
	}
}
