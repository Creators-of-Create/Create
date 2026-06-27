package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class DebugValueCommand {

	public static float value = 0;

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("debugValue")
			.requires(cs -> cs.hasPermission(4))
			.then(Commands.argument("value", FloatArgumentType.floatArg())
				.executes((ctx) -> {
					value = FloatArgumentType.getFloat(ctx, "value");
					ctx.getSource().sendSuccess(() -> {
						return Component.literal("Set value to: " + value);
					}, true);
					return 1;
				}));

	}
}
