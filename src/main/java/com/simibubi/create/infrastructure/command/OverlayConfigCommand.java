package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.content.equipment.goggles.GoggleConfigScreen;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.gui.ScreenOpener;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class OverlayConfigCommand {
	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("overlay")
			.requires(cs -> cs.hasPermission(0))
			.then(Commands.literal("reset")
				.executes(ctx -> {
					AllConfigs.client().overlayOffsetX.set(0);
					AllConfigs.client().overlayOffsetY.set(0);

					ctx.getSource().sendSuccess(() -> Component.literal("Create Goggle Overlay has been reset to default position"), true);
					return Command.SINGLE_SUCCESS;
				})
			)
			.executes(ctx -> {
				ScreenOpener.open(new GoggleConfigScreen());
				return Command.SINGLE_SUCCESS;
			});

	}
}
