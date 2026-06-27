package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.content.kinetics.KineticDebugger;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ToggleDebugCommand {
	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("rainbowDebug")
			.requires(cs -> cs.hasPermission(0))
			.then(Commands.argument("status", BoolArgumentType.bool())
				.executes(ctx -> {
					KineticDebugger.rainbowDebug = BoolArgumentType.getBool(ctx, "status");
					Component text = boolToText(KineticDebugger.rainbowDebug)
						.append(Component.literal(" Rainbow Debug Utility").withStyle(ChatFormatting.WHITE));

					ctx.getSource().sendSuccess(() -> text, false);
					return Command.SINGLE_SUCCESS;
				})
			)
			.executes(ctx -> {
				Component text = Component.literal("Rainbow Debug Utility is currently: ")
					.append(boolToText(KineticDebugger.rainbowDebug));

				ctx.getSource().sendSuccess(() -> text, false);
				return Command.SINGLE_SUCCESS;
			});
	}

	private static MutableComponent boolToText(boolean b) {
		if (b) {
			return Component.literal("enabled").withStyle(ChatFormatting.GREEN);
		} else {
			return Component.literal("disabled").withStyle(ChatFormatting.RED);
		}
	}
}
