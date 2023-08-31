package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;

import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class OverlayConfigCommand {

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("overlay")
				.requires(cs -> cs.hasPermission(0))
				.then(Commands.literal("reset")
					.executes(ctx -> {
						ServerPlayer player = ctx.getSource().getPlayerOrException();

						CatnipServices.NETWORK.simpleActionToClient(player, "overlayReset", "");

						ctx.getSource().sendSuccess(() -> Components.literal("Create Goggle Overlay has been reset to default position"), true);

						return Command.SINGLE_SUCCESS;
					})
				)
				.executes(ctx -> {
					ServerPlayer player = ctx.getSource().getPlayerOrException();

					CatnipServices.NETWORK.simpleActionToClient(player, "overlayScreen", "");

					return Command.SINGLE_SUCCESS;
				});

	}
}
