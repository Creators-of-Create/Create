package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;

import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.utility.Components;

import com.simibubi.create.infrastructure.debugInfo.DebugInformation;
import com.simibubi.create.infrastructure.debugInfo.ServerDebugInfoPacket;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import static net.minecraft.commands.Commands.literal;

public class DebugInfoCommand {
	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return literal("debuginfo").executes(ctx -> {
			CommandSourceStack source = ctx.getSource();
			ServerPlayer player = source.getPlayerOrException();
			source.sendSuccess(
					Components.literal("Sending server debug information to your client..."), true
			);
			AllPackets.getChannel().send(
					PacketDistributor.PLAYER.with(() -> player),
					new ServerDebugInfoPacket(player)
			);
			return Command.SINGLE_SUCCESS;
		});
	}
}
