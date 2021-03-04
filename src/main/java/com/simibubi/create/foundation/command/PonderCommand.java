package com.simibubi.create.foundation.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.foundation.networking.AllPackets;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;

public class PonderCommand {

	static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("ponder")
				.requires(cs -> cs.hasPermissionLevel(0))
				.executes(ctx -> {
					ServerPlayerEntity player = ctx.getSource().asPlayer();

					AllPackets.channel.send(
							PacketDistributor.PLAYER.with(() -> player),
							new ConfigureConfigPacket(ConfigureConfigPacket.Actions.ponderIndex.name(), ""));

					return 1;
				});
	}
}
