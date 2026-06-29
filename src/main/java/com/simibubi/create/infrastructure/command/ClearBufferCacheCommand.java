package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.CreateClient;

import net.createmod.ponder.PonderClient;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ClearBufferCacheCommand {
	static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("clearRenderBuffers")
			.executes(ctx -> {
				PonderClient.invalidateRenderers();
				CreateClient.invalidateRenderers();

				ctx.getSource().sendSuccess(() -> Component.literal("Cleared rendering buffers."), true);
				return Command.SINGLE_SUCCESS;
			});
	}
}
