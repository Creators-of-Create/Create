package com.simibubi.create.foundation.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.foundation.networking.AllPackets;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

public class FabulousWarningCommand {

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("dismissFabulousWarning")
				.requires(AllCommands.SOURCE_IS_PLAYER)
				.executes(ctx -> {
					ServerPlayer player = ctx.getSource()
							.getPlayerOrException();

					AllPackets.channel.send(
							PacketDistributor.PLAYER.with(() -> player),
							new SConfigureConfigPacket(SConfigureConfigPacket.Actions.fabulousWarning.name(), "")
					);

					return Command.SINGLE_SUCCESS;
				});

	}
}
