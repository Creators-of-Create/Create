package com.simibubi.create.foundation.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.foundation.networking.AllPackets;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;

public class FabulousWarningCommand {

	public static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("dismissFabulousWarning")
				.requires(AllCommands.sourceIsPlayer)
				.executes(ctx -> {
					ServerPlayerEntity player = ctx.getSource()
							.getPlayerOrException();

					AllPackets.channel.send(
							PacketDistributor.PLAYER.with(() -> player),
							new SConfigureConfigPacket(SConfigureConfigPacket.Actions.fabulousWarning.name(), "")
					);

					return Command.SINGLE_SUCCESS;
				});

	}
}
