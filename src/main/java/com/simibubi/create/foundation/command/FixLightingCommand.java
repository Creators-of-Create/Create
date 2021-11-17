package com.simibubi.create.foundation.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.foundation.networking.AllPackets;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class FixLightingCommand {

	static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("fixLighting")
			.requires(cs -> cs.hasPermission(0))
			.executes(ctx -> {
				AllPackets.channel.sendToClient(new SConfigureConfigPacket(SConfigureConfigPacket.Actions.fixLighting.name(), String.valueOf(true)),
						(ServerPlayer) ctx.getSource().getEntity());

				ctx.getSource()
					.sendSuccess(
						new TextComponent("Forge's experimental block rendering pipeline is now enabled."), true);

				return 1;
			});
	}
}
