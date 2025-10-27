package com.simibubi.create.infrastructure.command;

import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.createmod.catnip.platform.CatnipServices;
import com.simibubi.create.Create;
import com.simibubi.create.infrastructure.debugInfo.ServerDebugInfoPacket;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class DebugInfoCommand {
	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return literal("debuginfo").executes(ctx -> {
			CommandSourceStack source = ctx.getSource();
			ServerPlayer player = source.getPlayerOrException();

			Create.lang().translate("command.debuginfo.sending")
				.sendChat(player);
			CatnipServices.NETWORK.sendToClient(player, new ServerDebugInfoPacket(player));
			return Command.SINGLE_SUCCESS;
		});
	}
}
