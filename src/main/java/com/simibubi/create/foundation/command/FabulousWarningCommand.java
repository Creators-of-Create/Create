package com.simibubi.create.foundation.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.foundation.networking.AllPackets;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class FabulousWarningCommand {

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("dismissFabulousWarning")
				.requires(AllCommands.SOURCE_IS_PLAYER)
				.executes(ctx -> {
					ServerPlayer player = ctx.getSource()
							.getPlayerOrException();

					AllPackets.channel.sendToClient(new SConfigureConfigPacket(SConfigureConfigPacket.Actions.fabulousWarning.name(), ""), player);

					return Command.SINGLE_SUCCESS;
				});

	}
}
