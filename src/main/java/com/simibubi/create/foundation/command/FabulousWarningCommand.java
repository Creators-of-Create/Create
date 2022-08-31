package com.simibubi.create.foundation.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;

import net.createmod.catnip.net.ClientboundSimpleActionPacket;
import net.createmod.catnip.platform.CatnipServices;
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

					CatnipServices.NETWORK.sendToPlayer(
							player,
							new ClientboundSimpleActionPacket("fabulousWarning", ""));

					return Command.SINGLE_SUCCESS;
				});

	}
}
