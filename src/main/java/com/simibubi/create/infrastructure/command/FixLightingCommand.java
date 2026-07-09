package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class FixLightingCommand {
	static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("fixLighting")
			.requires(AllCommands.hasPermission(0))
			.executes(ctx -> {
				Minecraft mc = Minecraft.getInstance();
				if (mc.level != null)
					mc.levelRenderer.invalidateCompiledGeometry(mc.level, mc.options, mc.gameRenderer.mainCamera(), mc.getBlockColors());

				ctx.getSource().sendSuccess(() -> Component.literal("Lighting renderer refreshed."), true);
				return Command.SINGLE_SUCCESS;
			});
	}
}
