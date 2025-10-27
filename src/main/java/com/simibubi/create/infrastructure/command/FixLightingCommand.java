package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.builder.ArgumentBuilder;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class FixLightingCommand {

	static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("fixLighting")
			.requires(cs -> cs.hasPermission(0))
			.executes(ctx -> {
				CatnipServices.NETWORK.simpleActionToClient(
						(ServerPlayer) ctx.getSource().getEntity(),
						"experimentalLighting",
						String.valueOf(true)
				);

				ctx.getSource()
					.sendSuccess(() ->
						Component.literal("NeoForge's experimental block rendering pipeline is now enabled."), true);

				return 1;
			});
	}
}
