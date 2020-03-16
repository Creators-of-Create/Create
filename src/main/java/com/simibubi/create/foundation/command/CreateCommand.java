package com.simibubi.create.foundation.command;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class CreateCommand {

	public CreateCommand(CommandDispatcher<CommandSource> dispatcher) {
		// KillTPSCommand.register(dispatcher); Commented out for release
		dispatcher.register(Commands.literal("create").then(ToggleDebugCommand.register()));
	}
}
