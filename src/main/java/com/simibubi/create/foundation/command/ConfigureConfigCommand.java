package com.simibubi.create.foundation.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;

public abstract class ConfigureConfigCommand {

	protected final String commandLiteral;

	ConfigureConfigCommand(String commandLiteral) {
		this.commandLiteral = commandLiteral;
	}

	ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal(this.commandLiteral)
			.requires(cs -> cs.hasPermission(0))
			.then(Commands.literal("on")
				.executes(ctx -> {
					ServerPlayerEntity player = ctx.getSource()
						.getPlayerOrException();
					sendPacket(player, String.valueOf(true));

					return Command.SINGLE_SUCCESS;
				}))
			.then(Commands.literal("off")
				.executes(ctx -> {
					ServerPlayerEntity player = ctx.getSource()
						.getPlayerOrException();
					sendPacket(player, String.valueOf(false));

					return Command.SINGLE_SUCCESS;
				}))
			.executes(ctx -> {
				ServerPlayerEntity player = ctx.getSource()
					.getPlayerOrException();
				sendPacket(player, "info");

				return Command.SINGLE_SUCCESS;
			});
	}

	protected abstract void sendPacket(ServerPlayerEntity player, String option);
}
