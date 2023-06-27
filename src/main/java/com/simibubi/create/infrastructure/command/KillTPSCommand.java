package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class KillTPSCommand {

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("killtps")
			.requires(cs -> cs.hasPermission(2))
			.executes(ctx -> {
				// killtps no arguments
				ctx.getSource()
					.sendSuccess(Lang.translateDirect("command.killTPSCommand.status.slowed_by.0",
						Create.LAGGER.isLagging() ? Create.LAGGER.getTickTime() : 0), true);
				if (Create.LAGGER.isLagging())
					ctx.getSource()
						.sendSuccess(Lang.translateDirect("command.killTPSCommand.status.usage.0"), true);
				else
					ctx.getSource()
						.sendSuccess(Lang.translateDirect("command.killTPSCommand.status.usage.1"), true);

				return 1;
			})
			.then(Commands.literal("start")
				.executes(ctx -> {
					// killtps start no time
					int tickTime = Create.LAGGER.getTickTime();
					if (tickTime > 0) {
						Create.LAGGER.setLagging(true);
						ctx.getSource()
							.sendSuccess((Lang.translateDirect("command.killTPSCommand.status.slowed_by.1", tickTime)),
								true);
						ctx.getSource()
							.sendSuccess(Lang.translateDirect("command.killTPSCommand.status.usage.0"), true);
					} else {
						ctx.getSource()
							.sendSuccess(Lang.translateDirect("command.killTPSCommand.status.usage.1"), true);
					}

					return 1;
				})
				.then(Commands.argument(Lang.translateDirect("command.killTPSCommand.argument.tickTime")
					.getString(), IntegerArgumentType.integer(1))
					.executes(ctx -> {
						// killtps start tickTime
						int tickTime = IntegerArgumentType.getInteger(ctx,
							Lang.translateDirect("command.killTPSCommand.argument.tickTime")
								.getString());
						Create.LAGGER.setTickTime(tickTime);
						Create.LAGGER.setLagging(true);
						ctx.getSource()
							.sendSuccess((Lang.translateDirect("command.killTPSCommand.status.slowed_by.1", tickTime)),
								true);
						ctx.getSource()
							.sendSuccess(Lang.translateDirect("command.killTPSCommand.status.usage.0"), true);

						return 1;
					})))
			.then(Commands.literal("stop")
				.executes(ctx -> {
					// killtps stop
					Create.LAGGER.setLagging(false);
					ctx.getSource()
						.sendSuccess(Lang.translateDirect("command.killTPSCommand.status.slowed_by.2"), false);

					return 1;
				}));
	}
}
