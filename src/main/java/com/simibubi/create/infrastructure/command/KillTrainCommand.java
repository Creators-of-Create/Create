package com.simibubi.create.infrastructure.command;

import java.util.UUID;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;

import net.createmod.catnip.utility.lang.Components;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;

public class KillTrainCommand {

	static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("killTrain")
			.requires(cs -> cs.hasPermission(2))
			.then(Commands.argument("train", UuidArgument.uuid())
				.executes(ctx -> {
					CommandSourceStack source = ctx.getSource();
					run(source, UuidArgument.getUuid(ctx, "train"));
					return 1;
				}));
	}

	private static void run(CommandSourceStack source, UUID argument) {
		Train train = Create.RAILWAYS.trains.get(argument);
		if (train == null) {
			source.sendFailure(Components.literal("No Train with id " + argument.toString()
				.substring(0, 5) + "[...] was found"));
			return;
		}

		train.invalid = true;
		source.sendSuccess(Components.literal("Train '").append(train.name)
			.append("' removed successfully"), true);
	}

}
