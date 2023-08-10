package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public abstract class ConfigureConfigCommand {

	protected final String commandLiteral;

	ConfigureConfigCommand(String commandLiteral) {
		this.commandLiteral = commandLiteral;
	}

	ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal(this.commandLiteral)
			.requires(cs -> cs.hasPermission(0))
			.then(Commands.literal("on")
				.executes(ctx -> {
					ServerPlayer player = ctx.getSource()
						.getPlayerOrException();
					sendPacket(player, String.valueOf(true));

					return Command.SINGLE_SUCCESS;
				}))
			.then(Commands.literal("off")
				.executes(ctx -> {
					ServerPlayer player = ctx.getSource()
						.getPlayerOrException();
					sendPacket(player, String.valueOf(false));

					return Command.SINGLE_SUCCESS;
				}))
			.executes(ctx -> {
				ServerPlayer player = ctx.getSource()
					.getPlayerOrException();
				sendPacket(player, "info");

				return Command.SINGLE_SUCCESS;
			});
	}

	protected abstract void sendPacket(ServerPlayer player, String option);
}
