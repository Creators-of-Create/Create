package com.simibubi.create.foundation.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.CreateClient;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.tterrag.registrate.fabric.EnvExecutor;

public class ClearBufferCacheCommand {

	static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("clearRenderBuffers")
			.requires(cs -> cs.hasPermission(0))
			.executes(ctx -> {
				EnvExecutor.runWhenOn(EnvType.CLIENT, () -> ClearBufferCacheCommand::execute);
				ctx.getSource()
					.sendSuccess(new TextComponent("Cleared rendering buffers."), true);
				return 1;
			});
	}

	@Environment(EnvType.CLIENT)
	private static void execute() {
		CreateClient.invalidateRenderers();
	}
}
