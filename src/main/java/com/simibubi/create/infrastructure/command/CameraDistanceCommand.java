package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.AllPackets;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public class CameraDistanceCommand {

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("camera")
				.then(Commands.literal("reset")
						.executes(ctx -> {
							ServerPlayer player = ctx.getSource().getPlayerOrException();
							AllPackets.getChannel().send(
									PacketDistributor.PLAYER.with(() -> player),
									new SConfigureConfigPacket(SConfigureConfigPacket.Actions.zoomMultiplier.name(), "1")
							);

							return Command.SINGLE_SUCCESS;
						})
				).then(Commands.argument("multiplier", FloatArgumentType.floatArg(0))
						.executes(ctx -> {
							float multiplier = FloatArgumentType.getFloat(ctx, "multiplier");
							ServerPlayer player = ctx.getSource().getPlayerOrException();
							AllPackets.getChannel().send(
									PacketDistributor.PLAYER.with(() -> player),
									new SConfigureConfigPacket(SConfigureConfigPacket.Actions.zoomMultiplier.name(), String.valueOf(multiplier))
							);

							return Command.SINGLE_SUCCESS;
						})
				);
	}

}
