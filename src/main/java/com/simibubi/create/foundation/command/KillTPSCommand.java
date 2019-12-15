package com.simibubi.create.foundation.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.simibubi.create.Create;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class KillTPSCommand {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
		Commands.literal("killtps")//todo replace String Components with Translation Components
			.requires(cs -> cs.hasPermissionLevel(2))
			.executes(ctx -> {
				//killtps no arguments
				ctx.getSource().sendFeedback(new StringTextComponent(String.format("[Create]: Server tick is currently slowed by %s ms",Create.lagger.isLagging() ? Create.lagger.getTickTime() : 0)), true);
				if (Create.lagger.isLagging())
					ctx.getSource().sendFeedback(new StringTextComponent("[Create]: use /killtps stop to bring back server tick to regular speed"), true);
				else
					ctx.getSource().sendFeedback(new StringTextComponent("[Create]: use /killtps start <tickTime> to artificially slow down the server tick"),true);

				return 1;
			})
			.then(Commands.literal("start")
				.executes(ctx -> {
					//killtps start no time
					int tickTime = Create.lagger.getTickTime();
					if (tickTime > 0){
						Create.lagger.setLagging(true);
						ctx.getSource().sendFeedback(new StringTextComponent(String.format("[Create]: Server tick is slowed by %s ms now :)", tickTime)),true);
						ctx.getSource().sendFeedback(new StringTextComponent("[Create]: use /killtps stop to bring back server tick to regular speed"),true);
					} else {
						ctx.getSource().sendFeedback(new StringTextComponent("[Create]: use /killtps start <tickTime> to artificially slow down the server tick"),true);
					}

					return 1;
				})
				.then(Commands.argument("tickTime", IntegerArgumentType.integer(1))
					.executes(ctx -> {
						//killtps start tickTime
						int tickTime = IntegerArgumentType.getInteger(ctx, "tickTime");
						Create.lagger.setTickTime(tickTime);
						Create.lagger.setLagging(true);
						ctx.getSource().sendFeedback(new StringTextComponent(String.format("[Create]: Server tick is slowed by %s ms now :)", tickTime)),true);
						ctx.getSource().sendFeedback(new StringTextComponent("[Create]: use /killtps stop to bring server tick to regular speed again"),true);

						return 1;
					})
				)
			)
			.then(Commands.literal("stop")
				.executes(ctx -> {
					//killtps stop
					Create.lagger.setLagging(false);
					ctx.getSource().sendFeedback(new StringTextComponent("[Create]: Server tick is back to regular speed"), false);

					return 1;
				})
			)
		);
	}
}
