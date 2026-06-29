package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import net.neoforged.neoforge.common.NeoForgeConfig;

public class FixLightingCommand {
	static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("fixLighting")
			.requires(cs -> cs.hasPermission(0))
			.executes(ctx -> {
				NeoForgeConfig.CLIENT.experimentalForgeLightPipelineEnabled.set(true);
				Minecraft.getInstance().levelRenderer.allChanged();

				ctx.getSource().sendSuccess(() -> Component.literal("NeoForge's experimental block rendering pipeline is now enabled."), true);
				return Command.SINGLE_SUCCESS;
			});
	}
}
