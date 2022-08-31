package com.simibubi.create.foundation.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;

import net.createmod.catnip.net.ClientboundSimpleActionPacket;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class CameraDistanceCommand {

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("camera")
				.then(Commands.literal("reset")
						.executes(ctx -> {
							ServerPlayer player = ctx.getSource().getPlayerOrException();
							CatnipServices.NETWORK.sendToPlayer(
									player,
									new ClientboundSimpleActionPacket("zoomMultiplier", ""));

							return Command.SINGLE_SUCCESS;
						})
				).then(Commands.argument("multiplier", FloatArgumentType.floatArg(0))
						.executes(ctx -> {
							float multiplier = FloatArgumentType.getFloat(ctx, "multiplier");
							ServerPlayer player = ctx.getSource().getPlayerOrException();
							CatnipServices.NETWORK.sendToPlayer(
									player,
									new ClientboundSimpleActionPacket("zoomMultiplier", String.valueOf(multiplier)));

							return Command.SINGLE_SUCCESS;
						})
				);
	}

}
