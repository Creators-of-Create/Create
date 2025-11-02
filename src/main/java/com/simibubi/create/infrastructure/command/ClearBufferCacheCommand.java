package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.CreateClient;

import net.createmod.catnip.platform.CatnipServices;
import net.createmod.ponder.PonderClient;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ClearBufferCacheCommand {

	static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("clearRenderBuffers")
			.requires(cs -> cs.hasPermission(0))
			.executes(ctx -> {
				CatnipServices.PLATFORM.executeOnClientOnly(() -> ClearBufferCacheCommand::execute);
				ctx.getSource()
						.sendSuccess(() -> Component.literal("Cleared rendering buffers."), true);
				return 1;
			});
	}

	@OnlyIn(Dist.CLIENT)
	private static void execute() {
		PonderClient.invalidateRenderers();
		CreateClient.invalidateRenderers();
	}
}
