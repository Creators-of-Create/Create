package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.foundation.utility.CameraAngleAnimationService;
import com.simibubi.create.foundation.utility.CameraAngleAnimationService.Mode;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;

import net.neoforged.neoforge.server.command.EnumArgument;

public class CameraAngleCommand {
	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("angle")
			.requires(cs -> cs.hasPermission(2))
			.then(Commands.argument("players", EntityArgument.players())
				.then(Commands.literal("yaw")
					.then(Commands.argument("degrees", FloatArgumentType.floatArg())
						.executes(ctx -> {
							float angleTarget = FloatArgumentType.getFloat(ctx, "degrees");
							CameraAngleAnimationService.setYawTarget(angleTarget);

							return Command.SINGLE_SUCCESS;
						})
					)
				).then(Commands.literal("pitch")
					.then(Commands.argument("degrees", FloatArgumentType.floatArg())
						.executes(ctx -> {
							float angleTarget = FloatArgumentType.getFloat(ctx, "degrees");
							CameraAngleAnimationService.setPitchTarget(angleTarget);

							return Command.SINGLE_SUCCESS;
						})
					)
				).then(Commands.literal("mode")
					.then(Commands.argument("mode", EnumArgument.enumArgument(Mode.class))
						.executes(ctx -> {
							Mode mode = ctx.getArgument("mode", Mode.class);

							CameraAngleAnimationService.setAnimationMode(mode);

							return Command.SINGLE_SUCCESS;
						})
						.then(Commands.argument("speed", FloatArgumentType.floatArg(0))
							.executes(ctx -> {
								Mode mode = ctx.getArgument("mode", Mode.class);
								float speed = FloatArgumentType.getFloat(ctx, "speed");

								CameraAngleAnimationService.setAnimationMode(mode);
								CameraAngleAnimationService.setAnimationSpeed(speed);

								return Command.SINGLE_SUCCESS;
							})
						))
				)
			);
	}
}
