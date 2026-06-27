package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.content.trains.CameraDistanceModifier;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CameraDistanceCommand {
	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("camera")
			.then(Commands.literal("reset")
				.executes(ctx -> {
					CameraDistanceModifier.zoomOut(1);

					return Command.SINGLE_SUCCESS;
				})
			).then(Commands.argument("multiplier", FloatArgumentType.floatArg(1))
				.executes(ctx -> {
					float multiplier = FloatArgumentType.getFloat(ctx, "multiplier");
					CameraDistanceModifier.zoomOut(multiplier);

					return Command.SINGLE_SUCCESS;
				})
			);
	}
}
