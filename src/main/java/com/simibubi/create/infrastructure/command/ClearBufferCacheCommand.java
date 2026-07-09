package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.CreateClient;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ClearBufferCacheCommand {
	static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("clearRenderBuffers")
			.executes(ctx -> {
				// TODO 26.2: Restore Ponder renderer invalidation once its public client API is available.
				CreateClient.invalidateRenderers();

				ctx.getSource().sendSuccess(() -> Component.literal("Cleared rendering buffers."), true);
				return Command.SINGLE_SUCCESS;
			});
	}
}
